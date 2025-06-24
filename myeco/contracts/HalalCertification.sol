pragma solidity ^0.8.19;

import "@openzeppelin/contracts/access/AccessControl.sol";
import "@openzeppelin/contracts/security/Pausable.sol";
import "@openzeppelin/contracts/utils/Counters.sol";

/**
 * @title HalalCertification
 * @dev Smart contract for managing Halal product certifications
 * @author HalalEco Team
 */
contract HalalCertification is AccessControl, Pausable {
    using Counters for Counters.Counter;
    
    // Roles
    bytes32 public constant CERTIFIER_ROLE = keccak256("CERTIFIER_ROLE");
    bytes32 public constant AUDITOR_ROLE = keccak256("AUDITOR_ROLE");
    bytes32 public constant REGULATOR_ROLE = keccak256("REGULATOR_ROLE");
    
    // Counters
    Counters.Counter private _certificationIds;
    Counters.Counter private _productIds;
    
    // Certification Status
    enum CertificationStatus {
        Active,
        Expired,
        Revoked,
        Suspended,
        UnderReview
    }
    
    // Certification Authority Types
    enum AuthorityType {
        Government,    // JAKIM, MUIS, etc.
        Private,       // HFA, IFANCA, etc.
        Community,     // Local Islamic organizations
        AI            // AI-powered validation
    }
    
    // Structs
    struct CertificationAuthority {
        string name;
        string country;
        AuthorityType authorityType;
        bool isActive;
        uint256 trustScore; // 0-100
        string contactInfo;
        uint256 registrationDate;
    }
    
    struct HalalCertification {
        uint256 certificationId;
        string productId;
        string productName;
        string[] ingredients;
        address certifier;
        string certificationNumber;
        uint256 issueDate;
        uint256 expiryDate;
        CertificationStatus status;
        string ipfsHash; // For storing detailed certification documents
        uint256 authorityId;
        string qrCode;
        bool isRevocable;
    }
    
    struct Product {
        uint256 productId;
        string name;
        string category;
        string manufacturer;
        string[] certificationIds;
        bool isActive;
        uint256 creationDate;
        string supplyChainHash; // Reference to supply chain data
    }
    
    struct SupplyChainRecord {
        string stage; // "sourcing", "processing", "packaging", "distribution"
        string location;
        uint256 timestamp;
        string certifierAddress;
        string notes;
        bool isCompliant;
    }
    
    // Mappings
    mapping(uint256 => CertificationAuthority) public authorities;
    mapping(uint256 => HalalCertification) public certifications;
    mapping(uint256 => Product) public products;
    mapping(string => uint256) public certificationNumberToId;
    mapping(string => uint256) public productNameToId;
    mapping(bytes32 => SupplyChainRecord[]) public supplyChainRecords;
    mapping(address => uint256[]) public certifierCertifications;
    mapping(string => bool) public revokedCertifications;
    
    // Events
    event CertificationIssued(
        uint256 indexed certificationId,
        string indexed productId,
        address indexed certifier,
        string certificationNumber,
        uint256 expiryDate
    );
    
    event CertificationRevoked(
        uint256 indexed certificationId,
        address indexed revoker,
        string reason,
        uint256 timestamp
    );
    
    event CertificationExpired(
        uint256 indexed certificationId,
        uint256 timestamp
    );
    
    event AuthorityRegistered(
        uint256 indexed authorityId,
        string name,
        AuthorityType authorityType
    );
    
    event ProductRegistered(
        uint256 indexed productId,
        string name,
        string manufacturer
    );
    
    event SupplyChainRecordAdded(
        bytes32 indexed supplyChainId,
        string stage,
        string location,
        bool isCompliant
    );
    
    event QRCodeGenerated(
        uint256 indexed certificationId,
        string qrCode
    );
    
    // Modifiers
    modifier onlyValidCertification(uint256 _certificationId) {
        require(_certificationId <= _certificationIds.current(), "Invalid certification ID");
        require(
            certifications[_certificationId].status == CertificationStatus.Active,
            "Certification is not active"
        );
        require(
            certifications[_certificationId].expiryDate > block.timestamp,
            "Certification has expired"
        );
        _;
    }
    
    modifier onlyAuthorizedCertifier(uint256 _authorityId) {
        require(authorities[_authorityId].isActive, "Authority is not active");
        require(hasRole(CERTIFIER_ROLE, msg.sender), "Not authorized to certify");
        _;
    }
    
    constructor() {
        _grantRole(DEFAULT_ADMIN_ROLE, msg.sender);
        _grantRole(CERTIFIER_ROLE, msg.sender);
        _grantRole(AUDITOR_ROLE, msg.sender);
        _grantRole(REGULATOR_ROLE, msg.sender);
    }
    
    /**
     * @dev Register a new certification authority
     */
    function registerAuthority(
        string memory _name,
        string memory _country,
        AuthorityType _authorityType,
        uint256 _trustScore,
        string memory _contactInfo
    ) external onlyRole(DEFAULT_ADMIN_ROLE) returns (uint256) {
        require(_trustScore <= 100, "Trust score must be <= 100");
        
        uint256 authorityId = _certificationIds.current();
        _certificationIds.increment();
        
        authorities[authorityId] = CertificationAuthority({
            name: _name,
            country: _country,
            authorityType: _authorityType,
            isActive: true,
            trustScore: _trustScore,
            contactInfo: _contactInfo,
            registrationDate: block.timestamp
        });
        
        emit AuthorityRegistered(authorityId, _name, _authorityType);
        return authorityId;
    }
    
    /**
     * @dev Issue a new Halal certification
     */
    function issueCertification(
        string memory _productId,
        string memory _productName,
        string[] memory _ingredients,
        string memory _certificationNumber,
        uint256 _expiryDate,
        string memory _ipfsHash,
        uint256 _authorityId,
        bool _isRevocable
    ) external onlyAuthorizedCertifier(_authorityId) whenNotPaused returns (uint256) {
        require(_expiryDate > block.timestamp, "Expiry date must be in the future");
        require(bytes(_certificationNumber).length > 0, "Certification number required");
        require(certificationNumberToId[_certificationNumber] == 0, "Certification number already exists");
        
        uint256 certificationId = _certificationIds.current();
        _certificationIds.increment();
        
        // Generate QR code data
        string memory qrCode = generateQRCode(certificationId, _certificationNumber);
        
        certifications[certificationId] = HalalCertification({
            certificationId: certificationId,
            productId: _productId,
            productName: _productName,
            ingredients: _ingredients,
            certifier: msg.sender,
            certificationNumber: _certificationNumber,
            issueDate: block.timestamp,
            expiryDate: _expiryDate,
            status: CertificationStatus.Active,
            ipfsHash: _ipfsHash,
            authorityId: _authorityId,
            qrCode: qrCode,
            isRevocable: _isRevocable
        });
        
        certificationNumberToId[_certificationNumber] = certificationId;
        certifierCertifications[msg.sender].push(certificationId);
        
        emit CertificationIssued(certificationId, _productId, msg.sender, _certificationNumber, _expiryDate);
        emit QRCodeGenerated(certificationId, qrCode);
        
        return certificationId;
    }
    
    /**
     * @dev Verify a certification by ID
     */
    function verifyCertification(uint256 _certificationId) 
        external 
        view 
        returns (
            bool isValid,
            CertificationStatus status,
            uint256 expiryDate,
            string memory productName,
            string memory certificationNumber,
            string memory authorityName
        ) 
    {
        if (_certificationId > _certificationIds.current()) {
            return (false, CertificationStatus.UnderReview, 0, "", "", "");
        }
        
        HalalCertification memory cert = certifications[_certificationId];
        CertificationAuthority memory authority = authorities[cert.authorityId];
        
        bool valid = (cert.status == CertificationStatus.Active) && 
                    (cert.expiryDate > block.timestamp) &&
                    !revokedCertifications[cert.certificationNumber];
        
        return (
            valid,
            cert.status,
            cert.expiryDate,
            cert.productName,
            cert.certificationNumber,
            authority.name
        );
    }
    
    /**
     * @dev Verify certification by QR code
     */
    function verifyByQRCode(string memory _qrCode) 
        external 
        view 
        returns (
            bool isValid,
            uint256 certificationId,
            string memory productName,
            uint256 expiryDate
        ) 
    {
        // Extract certification ID from QR code
        uint256 certId = extractCertificationIdFromQR(_qrCode);
        
        if (certId == 0 || certId > _certificationIds.current()) {
            return (false, 0, "", 0);
        }
        
        HalalCertification memory cert = certifications[certId];
        
        bool valid = (cert.status == CertificationStatus.Active) && 
                    (cert.expiryDate > block.timestamp) &&
                    !revokedCertifications[cert.certificationNumber] &&
                    keccak256(bytes(cert.qrCode)) == keccak256(bytes(_qrCode));
        
        return (valid, certId, cert.productName, cert.expiryDate);
    }
    
    /**
     * @dev Revoke a certification (only by authorized roles)
     */
    function revokeCertification(
        uint256 _certificationId,
        string memory _reason
    ) external onlyRole(REGULATOR_ROLE) {
        require(_certificationId <= _certificationIds.current(), "Invalid certification ID");
        require(certifications[_certificationId].isRevocable, "Certification is not revocable");
        
        certifications[_certificationId].status = CertificationStatus.Revoked;
        revokedCertifications[certifications[_certificationId].certificationNumber] = true;
        
        emit CertificationRevoked(_certificationId, msg.sender, _reason, block.timestamp);
    }
    
    /**
     * @dev Add supply chain record
     */
    function addSupplyChainRecord(
        string memory _productId,
        string memory _stage,
        string memory _location,
        string memory _certifierAddress,
        string memory _notes,
        bool _isCompliant
    ) external onlyRole(CERTIFIER_ROLE) {
        bytes32 supplyChainId = keccak256(abi.encodePacked(_productId));
        
        supplyChainRecords[supplyChainId].push(SupplyChainRecord({
            stage: _stage,
            location: _location,
            timestamp: block.timestamp,
            certifierAddress: _certifierAddress,
            notes: _notes,
            isCompliant: _isCompliant
        }));
        
        emit SupplyChainRecordAdded(supplyChainId, _stage, _location, _isCompliant);
    }
    
    /**
     * @dev Get supply chain records for a product
     */
    function getSupplyChainRecords(string memory _productId) 
        external 
        view 
        returns (SupplyChainRecord[] memory) 
    {
        bytes32 supplyChainId = keccak256(abi.encodePacked(_productId));
        return supplyChainRecords[supplyChainId];
    }
    
    /**
     * @dev Register a new product
     */
    function registerProduct(
        string memory _name,
        string memory _category,
        string memory _manufacturer,
        string memory _supplyChainHash
    ) external onlyRole(CERTIFIER_ROLE) returns (uint256) {
        require(productNameToId[_name] == 0, "Product already registered");
        
        uint256 productId = _productIds.current();
        _productIds.increment();
        
        string[] memory emptyCertifications;
        
        products[productId] = Product({
            productId: productId,
            name: _name,
            category: _category,
            manufacturer: _manufacturer,
            certificationIds: emptyCertifications,
            isActive: true,
            creationDate: block.timestamp,
            supplyChainHash: _supplyChainHash
        });
        
        productNameToId[_name] = productId;
        
        emit ProductRegistered(productId, _name, _manufacturer);
        return productId;
    }
    
    /**
     * @dev Get certification details
     */
    function getCertificationDetails(uint256 _certificationId) 
        external 
        view 
        returns (
            string memory productName,
            string[] memory ingredients,
            address certifier,
            string memory certificationNumber,
            uint256 issueDate,
            uint256 expiryDate,
            CertificationStatus status,
            string memory authorityName
        ) 
    {
        require(_certificationId <= _certificationIds.current(), "Invalid certification ID");
        
        HalalCertification memory cert = certifications[_certificationId];
        CertificationAuthority memory authority = authorities[cert.authorityId];
        
        return (
            cert.productName,
            cert.ingredients,
            cert.certifier,
            cert.certificationNumber,
            cert.issueDate,
            cert.expiryDate,
            cert.status,
            authority.name
        );
    }
    
    /**
     * @dev Check if certification is expired and update status
     */
    function checkAndUpdateExpiredCertifications(uint256[] memory _certificationIds) 
        external 
        onlyRole(AUDITOR_ROLE) 
    {
        for (uint256 i = 0; i < _certificationIds.length; i++) {
            uint256 certId = _certificationIds[i];
            if (certId <= _certificationIds.current()) {
                HalalCertification storage cert = certifications[certId];
                if (cert.expiryDate <= block.timestamp && cert.status == CertificationStatus.Active) {
                    cert.status = CertificationStatus.Expired;
                    emit CertificationExpired(certId, block.timestamp);
                }
            }
        }
    }
    
    /**
     * @dev Generate QR code for certification
     */
    function generateQRCode(uint256 _certificationId, string memory _certificationNumber) 
        internal 
        pure 
        returns (string memory) 
    {
        return string(abi.encodePacked(
            "HALAL-CERT-",
            Strings.toString(_certificationId),
            "-",
            _certificationNumber
        ));
    }
    
    /**
     * @dev Extract certification ID from QR code
     */
    function extractCertificationIdFromQR(string memory _qrCode) 
        internal 
        pure 
        returns (uint256) 
    {
        // Simple extraction - in production, use more robust parsing
        bytes memory qrBytes = bytes(_qrCode);
        if (qrBytes.length < 12) return 0; // "HALAL-CERT-" is 11 chars
        
        // This is a simplified version - implement proper parsing
        return 1; // Placeholder
    }
    
    /**
     * @dev Get total number of certifications
     */
    function getTotalCertifications() external view returns (uint256) {
        return _certificationIds.current();
    }
    
    /**
     * @dev Get certifications by certifier
     */
    function getCertificationsByCertifier(address _certifier) 
        external 
        view 
        returns (uint256[] memory) 
    {
        return certifierCertifications[_certifier];
    }
    
    /**
     * @dev Pause contract (emergency)
     */
    function pause() external onlyRole(DEFAULT_ADMIN_ROLE) {
        _pause();
    }
    
    /**
     * @dev Unpause contract
     */
    function unpause() external onlyRole(DEFAULT_ADMIN_ROLE) {
        _unpause();
    }
}

// Helper library for string operations
library Strings {
    function toString(uint256 value) internal pure returns (string memory) {
        if (value == 0) {
            return "0";
        }
        uint256 temp = value;
        uint256 digits;
        while (temp != 0) {
            digits++;
            temp /= 10;
        }
        bytes memory buffer = new bytes(digits);
        while (value != 0) {
            digits -= 1;
            buffer[digits] = bytes1(uint8(48 + uint256(value % 10)));
            value /= 10;
        }
        return string(buffer);
    }
}
