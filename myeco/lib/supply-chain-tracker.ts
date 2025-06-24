import { AlchemyService } from "./alchemy"

export interface SupplyChainStage {
  id: string
  name: string
  location: string
  timestamp: number
  certifier: string
  status: "compliant" | "non_compliant" | "pending" | "flagged"
  documents: Document[]
  halalCompliance: HalalComplianceCheck
  environmentalData?: EnvironmentalData
  qualityMetrics?: QualityMetrics
}

export interface Document {
  type: "certificate" | "invoice" | "inspection_report" | "photo" | "video"
  url: string
  hash: string
  verified: boolean
  uploadedBy: string
  timestamp: number
}

export interface HalalComplianceCheck {
  isCompliant: boolean
  certificationId?: string
  inspector: string
  inspectionDate: number
  issues: string[]
  correctionActions: string[]
  nextInspectionDue?: number
}

export interface EnvironmentalData {
  temperature: number
  humidity: number
  storageConditions: string
  transportConditions: string
  contaminationRisk: "low" | "medium" | "high"
}

export interface QualityMetrics {
  freshness: number // 0-100
  appearance: number // 0-100
  packaging: number // 0-100
  overallQuality: number // 0-100
}

export interface SupplyChainRecord {
  productId: string
  productName: string
  batchNumber: string
  stages: SupplyChainStage[]
  currentStage: string
  overallCompliance: boolean
  riskScore: number
  alerts: Alert[]
  blockchainHash: string
  qrCode: string
  createdAt: number
  updatedAt: number
}

export interface Alert {
  id: string
  type: "contamination" | "temperature" | "certification" | "delay"
  severity: "low" | "medium" | "high" | "critical"
  message: string
  stage: string
  timestamp: number
  resolved: boolean
  resolutionNotes?: string
}

export interface TrackingQuery {
  productId?: string
  batchNumber?: string
  qrCode?: string
  blockchainHash?: string
}

export interface SupplyChainAnalytics {
  totalProducts: number
  compliantProducts: number
  complianceRate: number
  averageRiskScore: number
  stagePerformance: StagePerformance[]
  commonIssues: IssueFrequency[]
  trendAnalysis: TrendData[]
}

export interface StagePerformance {
  stageName: string
  complianceRate: number
  averageProcessingTime: number
  issueCount: number
}

export interface IssueFrequency {
  issue: string
  frequency: number
  impact: "low" | "medium" | "high"
}

export interface TrendData {
  date: string
  complianceRate: number
  riskScore: number
  alertCount: number
}

export class SupplyChainTracker {
  private static readonly STAGE_TEMPLATES = {
    sourcing: {
      name: "Raw Material Sourcing",
      requiredDocuments: ["certificate", "invoice"],
      complianceChecks: ["halal_certification", "origin_verification"],
      maxDuration: 7, // days
    },
    processing: {
      name: "Processing & Manufacturing",
      requiredDocuments: ["inspection_report", "certificate"],
      complianceChecks: ["facility_certification", "process_validation"],
      maxDuration: 3,
    },
    packaging: {
      name: "Packaging & Labeling",
      requiredDocuments: ["inspection_report", "photo"],
      complianceChecks: ["label_verification", "packaging_integrity"],
      maxDuration: 1,
    },
    distribution: {
      name: "Distribution & Storage",
      requiredDocuments: ["invoice", "photo"],
      complianceChecks: ["storage_conditions", "transport_verification"],
      maxDuration: 14,
    },
    retail: {
      name: "Retail & Sale",
      requiredDocuments: ["photo"],
      complianceChecks: ["display_compliance", "final_verification"],
      maxDuration: 30,
    },
  }

  static async createSupplyChainRecord(
    productId: string,
    productName: string,
    batchNumber: string,
  ): Promise<SupplyChainRecord> {
    try {
      const record: SupplyChainRecord = {
        productId,
        productName,
        batchNumber,
        stages: [],
        currentStage: "sourcing",
        overallCompliance: true,
        riskScore: 0,
        alerts: [],
        blockchainHash: "",
        qrCode: "",
        createdAt: Date.now(),
        updatedAt: Date.now(),
      }

      // Generate QR code
      record.qrCode = this.generateQRCode(productId, batchNumber)

      // Create blockchain record
      record.blockchainHash = await this.createBlockchainRecord(record)

      return record
    } catch (error) {
      console.error("Failed to create supply chain record:", error)
      throw new Error("Supply chain tracking initialization failed")
    }
  }

  static async addStage(recordId: string, stageData: Partial<SupplyChainStage>): Promise<SupplyChainRecord> {
    try {
      // In production, retrieve from database
      const record = await this.getRecord(recordId)

      const stage: SupplyChainStage = {
        id: `${recordId}-${Date.now()}`,
        name: stageData.name || "Unknown Stage",
        location: stageData.location || "Unknown Location",
        timestamp: Date.now(),
        certifier: stageData.certifier || "Unknown Certifier",
        status: "pending",
        documents: stageData.documents || [],
        halalCompliance: stageData.halalCompliance || {
          isCompliant: false,
          inspector: "Pending",
          inspectionDate: Date.now(),
          issues: [],
          correctionActions: [],
        },
        environmentalData: stageData.environmentalData,
        qualityMetrics: stageData.qualityMetrics,
      }

      // Validate stage compliance
      stage.status = await this.validateStageCompliance(stage)

      // Add stage to record
      record.stages.push(stage)
      record.updatedAt = Date.now()

      // Update overall compliance and risk score
      record.overallCompliance = this.calculateOverallCompliance(record.stages)
      record.riskScore = this.calculateRiskScore(record.stages)

      // Check for alerts
      const newAlerts = await this.checkForAlerts(stage, record)
      record.alerts.push(...newAlerts)

      // Update blockchain
      await this.updateBlockchainRecord(record)

      return record
    } catch (error) {
      console.error("Failed to add supply chain stage:", error)
      throw new Error("Supply chain stage addition failed")
    }
  }

  static async trackProduct(query: TrackingQuery): Promise<SupplyChainRecord | null> {
    try {
      // In production, query from database/blockchain
      if (query.qrCode) {
        return this.trackByQRCode(query.qrCode)
      }

      if (query.blockchainHash) {
        return this.trackByBlockchain(query.blockchainHash)
      }

      if (query.productId || query.batchNumber) {
        return this.trackByProductId(query.productId!, query.batchNumber)
      }

      return null
    } catch (error) {
      console.error("Product tracking failed:", error)
      return null
    }
  }

  static async detectContamination(
    recordId: string,
    contaminationData: {
      type: string
      severity: "low" | "medium" | "high" | "critical"
      affectedStages: string[]
      description: string
    },
  ): Promise<Alert[]> {
    try {
      const alerts: Alert[] = []

      // Create contamination alert
      const alert: Alert = {
        id: `CONT-${Date.now()}`,
        type: "contamination",
        severity: contaminationData.severity,
        message: `Contamination detected: ${contaminationData.description}`,
        stage: contaminationData.affectedStages[0],
        timestamp: Date.now(),
        resolved: false,
      }

      alerts.push(alert)

      // If critical, create alerts for all downstream stages
      if (contaminationData.severity === "critical") {
        contaminationData.affectedStages.forEach((stage) => {
          alerts.push({
            id: `CONT-${Date.now()}-${stage}`,
            type: "contamination",
            severity: "high",
            message: `Potential contamination in ${stage} stage`,
            stage,
            timestamp: Date.now(),
            resolved: false,
          })
        })
      }

      // Update blockchain with contamination record
      await this.recordContaminationOnBlockchain(recordId, contaminationData)

      return alerts
    } catch (error) {
      console.error("Contamination detection failed:", error)
      throw new Error("Contamination detection service failed")
    }
  }

  static async generateAnalytics(dateRange: { start: number; end: number }): Promise<SupplyChainAnalytics> {
    try {
      // In production, query from database
      const mockData: SupplyChainAnalytics = {
        totalProducts: 1250,
        compliantProducts: 1187,
        complianceRate: 94.96,
        averageRiskScore: 2.3,
        stagePerformance: [
          {
            stageName: "Sourcing",
            complianceRate: 98.2,
            averageProcessingTime: 5.2,
            issueCount: 12,
          },
          {
            stageName: "Processing",
            complianceRate: 96.8,
            averageProcessingTime: 2.8,
            issueCount: 18,
          },
          {
            stageName: "Packaging",
            complianceRate: 99.1,
            averageProcessingTime: 0.8,
            issueCount: 5,
          },
          {
            stageName: "Distribution",
            complianceRate: 94.5,
            averageProcessingTime: 8.2,
            issueCount: 32,
          },
          {
            stageName: "Retail",
            complianceRate: 97.3,
            averageProcessingTime: 15.5,
            issueCount: 15,
          },
        ],
        commonIssues: [
          { issue: "Temperature deviation", frequency: 28, impact: "medium" },
          { issue: "Documentation delay", frequency: 22, impact: "low" },
          { issue: "Certification expiry", frequency: 15, impact: "high" },
          { issue: "Storage conditions", frequency: 12, impact: "medium" },
          { issue: "Transport delay", frequency: 8, impact: "low" },
        ],
        trendAnalysis: this.generateTrendData(dateRange),
      }

      return mockData
    } catch (error) {
      console.error("Analytics generation failed:", error)
      throw new Error("Supply chain analytics service failed")
    }
  }

  private static async validateStageCompliance(
    stage: SupplyChainStage,
  ): Promise<"compliant" | "non_compliant" | "pending" | "flagged"> {
    // Check required documents
    const template =
      this.STAGE_TEMPLATES[stage.name.toLowerCase().replace(/\s+/g, "_") as keyof typeof this.STAGE_TEMPLATES]

    if (!template) return "pending"

    const hasRequiredDocs = template.requiredDocuments.every((docType) =>
      stage.documents.some((doc) => doc.type === docType && doc.verified),
    )

    if (!hasRequiredDocs) return "non_compliant"

    // Check Halal compliance
    if (!stage.halalCompliance.isCompliant) return "non_compliant"

    // Check environmental conditions
    if (stage.environmentalData) {
      if (stage.environmentalData.contaminationRisk === "high") return "flagged"
      if (stage.environmentalData.temperature < 0 || stage.environmentalData.temperature > 40) {
        return "flagged"
      }
    }

    return "compliant"
  }

  private static calculateOverallCompliance(stages: SupplyChainStage[]): boolean {
    return stages.every((stage) => stage.status === "compliant" || stage.status === "pending")
  }

  private static calculateRiskScore(stages: SupplyChainStage[]): number {
    let totalRisk = 0
    let stageCount = 0

    stages.forEach((stage) => {
      let stageRisk = 0

      // Status risk
      switch (stage.status) {
        case "non_compliant":
          stageRisk += 4
          break
        case "flagged":
          stageRisk += 3
          break
        case "pending":
          stageRisk += 1
          break
        default:
          stageRisk += 0
      }

      // Environmental risk
      if (stage.environmentalData) {
        switch (stage.environmentalData.contaminationRisk) {
          case "high":
            stageRisk += 3
            break
          case "medium":
            stageRisk += 2
            break
          case "low":
            stageRisk += 1
            break
        }
      }

      // Compliance issues risk
      stageRisk += stage.halalCompliance.issues.length * 0.5

      totalRisk += stageRisk
      stageCount++
    })

    return stageCount > 0 ? Math.min(10, totalRisk / stageCount) : 0
  }

  private static async checkForAlerts(stage: SupplyChainStage, record: SupplyChainRecord): Promise<Alert[]> {
    const alerts: Alert[] = []

    // Temperature alerts
    if (stage.environmentalData) {
      if (stage.environmentalData.temperature > 35) {
        alerts.push({
          id: `TEMP-${Date.now()}`,
          type: "temperature",
          severity: "high",
          message: `High temperature detected: ${stage.environmentalData.temperature}°C`,
          stage: stage.name,
          timestamp: Date.now(),
          resolved: false,
        })
      }
    }

    // Certification alerts
    if (stage.halalCompliance.nextInspectionDue) {
      const daysUntilDue = (stage.halalCompliance.nextInspectionDue - Date.now()) / (1000 * 60 * 60 * 24)
      if (daysUntilDue <= 7) {
        alerts.push({
          id: `CERT-${Date.now()}`,
          type: "certification",
          severity: daysUntilDue <= 1 ? "critical" : "medium",
          message: `Certification inspection due in ${Math.ceil(daysUntilDue)} days`,
          stage: stage.name,
          timestamp: Date.now(),
          resolved: false,
        })
      }
    }

    // Delay alerts
    const template =
      this.STAGE_TEMPLATES[stage.name.toLowerCase().replace(/\s+/g, "_") as keyof typeof this.STAGE_TEMPLATES]
    if (template) {
      const stageAge = (Date.now() - stage.timestamp) / (1000 * 60 * 60 * 24)
      if (stageAge > template.maxDuration) {
        alerts.push({
          id: `DELAY-${Date.now()}`,
          type: "delay",
          severity: "medium",
          message: `Stage processing time exceeded: ${stageAge.toFixed(1)} days`,
          stage: stage.name,
          timestamp: Date.now(),
          resolved: false,
        })
      }
    }

    return alerts
  }

  private static generateQRCode(productId: string, batchNumber: string): string {
    return `HALAL-SC-${productId}-${batchNumber}-${Date.now()}`
  }

  private static async createBlockchainRecord(record: SupplyChainRecord): Promise<string> {
    try {
      const result = await AlchemyService.createHalalCertificationRecord({
        productId: record.productId,
        certificationId: `SC-${record.batchNumber}`,
        authority: "HalalEco Supply Chain",
        expiryDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString(),
      })

      return result.transactionHash || `0x${Buffer.from(JSON.stringify(record)).toString("hex").slice(0, 64)}`
    } catch (error) {
      return `0x${Buffer.from(JSON.stringify(record)).toString("hex").slice(0, 64)}`
    }
  }

  private static async updateBlockchainRecord(record: SupplyChainRecord): Promise<void> {
    // In production, update blockchain with new stage data
    console.log("Updating blockchain record:", record.blockchainHash)
  }

  private static async recordContaminationOnBlockchain(recordId: string, contaminationData: any): Promise<void> {
    // In production, record contamination event on blockchain
    console.log("Recording contamination on blockchain:", recordId, contaminationData)
  }

  private static async getRecord(recordId: string): Promise<SupplyChainRecord> {
    // Mock record retrieval - in production, get from database
    return {
      productId: recordId,
      productName: "Sample Product",
      batchNumber: "BATCH-001",
      stages: [],
      currentStage: "sourcing",
      overallCompliance: true,
      riskScore: 0,
      alerts: [],
      blockchainHash: "0x123...",
      qrCode: "HALAL-SC-123",
      createdAt: Date.now() - 86400000,
      updatedAt: Date.now(),
    }
  }

  private static async trackByQRCode(qrCode: string): Promise<SupplyChainRecord | null> {
    // Mock QR code tracking
    return this.getRecord("sample-product-id")
  }

  private static async trackByBlockchain(hash: string): Promise<SupplyChainRecord | null> {
    // Mock blockchain tracking
    return this.getRecord("sample-product-id")
  }

  private static async trackByProductId(productId: string, batchNumber?: string): Promise<SupplyChainRecord | null> {
    // Mock product ID tracking
    return this.getRecord(productId)
  }

  private static generateTrendData(dateRange: { start: number; end: number }): TrendData[] {
    const trends: TrendData[] = []
    const days = Math.ceil((dateRange.end - dateRange.start) / (1000 * 60 * 60 * 24))

    for (let i = 0; i < Math.min(days, 30); i++) {
      const date = new Date(dateRange.start + i * 24 * 60 * 60 * 1000)
      trends.push({
        date: date.toISOString().split("T")[0],
        complianceRate: 90 + Math.random() * 10,
        riskScore: 1 + Math.random() * 3,
        alertCount: Math.floor(Math.random() * 10),
      })
    }

    return trends
  }
}
