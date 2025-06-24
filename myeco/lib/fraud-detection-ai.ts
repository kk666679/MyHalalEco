export interface FraudDetectionRequest {
  productId: string
  productName: string
  price: string
  sellerRating: number
  sellerHistory: {
    accountAge: number // days
    totalSales: number
    returnRate: number // percentage
    complaintCount: number
  }
  certificationImage?: string
  ingredients: string[]
  productImages: string[]
  description: string
  category: string
  supplier?: string
  location?: string
}

export interface FraudDetectionResponse {
  riskScore: number // 1-10
  riskLevel: "low" | "medium" | "high" | "critical"
  redFlags: RedFlag[]
  recommendedAction: "approve" | "flag" | "block" | "manual_review"
  confidence: number // 0-100
  fraudProbability: number // 0-100
  detailedAnalysis: {
    priceAnalysis: PriceAnalysis
    sellerAnalysis: SellerAnalysis
    imageAnalysis: ImageAnalysis
    textAnalysis: TextAnalysis
    certificationAnalysis: CertificationAnalysis
  }
  recommendations: string[]
}

export interface RedFlag {
  type: "price" | "seller" | "image" | "text" | "certification" | "pattern"
  severity: "low" | "medium" | "high" | "critical"
  description: string
  evidence: string
  impact: number // 1-5
}

export interface PriceAnalysis {
  marketPrice: number
  priceDeviation: number // percentage
  isPriceSuspicious: boolean
  priceCategory: "very_low" | "low" | "normal" | "high" | "very_high"
  competitorPrices: number[]
}

export interface SellerAnalysis {
  trustScore: number // 0-100
  riskFactors: string[]
  accountFlags: string[]
  behaviorPattern: "normal" | "suspicious" | "fraudulent"
  verificationStatus: boolean
}

export interface ImageAnalysis {
  isAuthentic: boolean
  duplicateDetected: boolean
  qualityScore: number // 0-100
  manipulationDetected: boolean
  certificationImageValid: boolean
  suspiciousElements: string[]
}

export interface TextAnalysis {
  languageQuality: number // 0-100
  grammarScore: number // 0-100
  suspiciousKeywords: string[]
  claimsVerification: ClaimVerification[]
  sentimentScore: number // -1 to 1
}

export interface ClaimVerification {
  claim: string
  isVerifiable: boolean
  confidence: number
  evidence?: string
}

export interface CertificationAnalysis {
  hasValidCertification: boolean
  certificationAuthority: string
  certificationExpiry?: string
  imageAuthenticity: number // 0-100
  blockchainVerified: boolean
  suspiciousElements: string[]
}

export class FraudDetectionAI {
  private static readonly SUSPICIOUS_KEYWORDS = [
    "guaranteed halal",
    "100% authentic",
    "limited time",
    "urgent sale",
    "no questions asked",
    "cash only",
    "final sale",
    "as seen on tv",
    "miracle",
    "instant results",
    "secret formula",
    "government approved",
  ]

  private static readonly HIGH_RISK_PATTERNS = [
    "new seller with premium products",
    "prices significantly below market",
    "poor quality images",
    "generic product descriptions",
    "no certification images",
    "multiple similar listings",
  ]

  static async analyzeForFraud(request: FraudDetectionRequest): Promise<FraudDetectionResponse> {
    try {
      // Perform comprehensive fraud analysis
      const priceAnalysis = await this.analyzePricing(request)
      const sellerAnalysis = this.analyzeSellerProfile(request)
      const imageAnalysis = await this.analyzeImages(request)
      const textAnalysis = this.analyzeTextContent(request)
      const certificationAnalysis = await this.analyzeCertification(request)

      // Calculate overall risk score
      const riskScore = this.calculateRiskScore({
        priceAnalysis,
        sellerAnalysis,
        imageAnalysis,
        textAnalysis,
        certificationAnalysis,
      })

      // Identify red flags
      const redFlags = this.identifyRedFlags({
        priceAnalysis,
        sellerAnalysis,
        imageAnalysis,
        textAnalysis,
        certificationAnalysis,
      })

      // Determine risk level and action
      const riskLevel = this.determineRiskLevel(riskScore)
      const recommendedAction = this.determineAction(riskScore, redFlags)
      const confidence = this.calculateConfidence(redFlags)
      const fraudProbability = this.calculateFraudProbability(riskScore, redFlags)

      // Generate recommendations
      const recommendations = this.generateRecommendations(redFlags, riskLevel)

      return {
        riskScore,
        riskLevel,
        redFlags,
        recommendedAction,
        confidence,
        fraudProbability,
        detailedAnalysis: {
          priceAnalysis,
          sellerAnalysis,
          imageAnalysis,
          textAnalysis,
          certificationAnalysis,
        },
        recommendations,
      }
    } catch (error) {
      console.error("Fraud detection analysis failed:", error)
      throw new Error("Fraud detection service temporarily unavailable")
    }
  }

  private static async analyzePricing(request: FraudDetectionRequest): Promise<PriceAnalysis> {
    const price = Number.parseFloat(request.price.replace(/[^0-9.]/g, ""))

    // Simulate market price analysis (in production, use real market data)
    const marketPrice = this.estimateMarketPrice(request.category, request.productName)
    const priceDeviation = ((marketPrice - price) / marketPrice) * 100

    const isPriceSuspicious = Math.abs(priceDeviation) > 50 // More than 50% deviation

    let priceCategory: "very_low" | "low" | "normal" | "high" | "very_high" = "normal"
    if (priceDeviation > 75) priceCategory = "very_low"
    else if (priceDeviation > 25) priceCategory = "low"
    else if (priceDeviation < -75) priceCategory = "very_high"
    else if (priceDeviation < -25) priceCategory = "high"

    // Simulate competitor prices
    const competitorPrices = this.generateCompetitorPrices(marketPrice)

    return {
      marketPrice,
      priceDeviation,
      isPriceSuspicious,
      priceCategory,
      competitorPrices,
    }
  }

  private static analyzeSellerProfile(request: FraudDetectionRequest): SellerAnalysis {
    const { sellerRating, sellerHistory } = request
    const riskFactors: string[] = []
    const accountFlags: string[] = []

    // Calculate trust score
    let trustScore = 50 // Base score

    // Rating analysis
    if (sellerRating < 2.0) {
      trustScore -= 30
      riskFactors.push("Very low seller rating")
    } else if (sellerRating < 3.5) {
      trustScore -= 15
      riskFactors.push("Below average seller rating")
    } else if (sellerRating > 4.5) {
      trustScore += 10
    }

    // Account age analysis
    if (sellerHistory.accountAge < 30) {
      trustScore -= 20
      riskFactors.push("New seller account")
      accountFlags.push("Account less than 30 days old")
    } else if (sellerHistory.accountAge < 90) {
      trustScore -= 10
      riskFactors.push("Relatively new seller")
    }

    // Sales history analysis
    if (sellerHistory.totalSales < 10) {
      trustScore -= 15
      riskFactors.push("Limited sales history")
    } else if (sellerHistory.totalSales > 1000) {
      trustScore += 15
    }

    // Return rate analysis
    if (sellerHistory.returnRate > 20) {
      trustScore -= 25
      riskFactors.push("High return rate")
      accountFlags.push(`Return rate: ${sellerHistory.returnRate}%`)
    } else if (sellerHistory.returnRate > 10) {
      trustScore -= 10
      riskFactors.push("Above average return rate")
    }

    // Complaint analysis
    if (sellerHistory.complaintCount > 10) {
      trustScore -= 20
      riskFactors.push("Multiple customer complaints")
      accountFlags.push(`${sellerHistory.complaintCount} complaints`)
    } else if (sellerHistory.complaintCount > 5) {
      trustScore -= 10
      riskFactors.push("Some customer complaints")
    }

    // Determine behavior pattern
    let behaviorPattern: "normal" | "suspicious" | "fraudulent" = "normal"
    if (trustScore < 30) behaviorPattern = "fraudulent"
    else if (trustScore < 50) behaviorPattern = "suspicious"

    // Verification status (simplified)
    const verificationStatus = sellerHistory.accountAge > 90 && sellerHistory.totalSales > 50

    return {
      trustScore: Math.max(0, Math.min(100, trustScore)),
      riskFactors,
      accountFlags,
      behaviorPattern,
      verificationStatus,
    }
  }

  private static async analyzeImages(request: FraudDetectionRequest): Promise<ImageAnalysis> {
    // Simulate image analysis (in production, use computer vision APIs)
    const suspiciousElements: string[] = []

    // Basic image quality analysis
    const qualityScore = Math.random() * 40 + 60 // Simulate 60-100 quality score

    // Duplicate detection simulation
    const duplicateDetected = Math.random() < 0.1 // 10% chance of duplicate
    if (duplicateDetected) {
      suspiciousElements.push("Image appears in multiple listings")
    }

    // Manipulation detection
    const manipulationDetected = Math.random() < 0.05 // 5% chance of manipulation
    if (manipulationDetected) {
      suspiciousElements.push("Possible image manipulation detected")
    }

    // Certification image validation
    const certificationImageValid = request.certificationImage ? Math.random() > 0.2 : false
    if (request.certificationImage && !certificationImageValid) {
      suspiciousElements.push("Certification image appears invalid or altered")
    }

    // Image authenticity
    const isAuthentic = !duplicateDetected && !manipulationDetected && qualityScore > 70

    return {
      isAuthentic,
      duplicateDetected,
      qualityScore,
      manipulationDetected,
      certificationImageValid,
      suspiciousElements,
    }
  }

  private static analyzeTextContent(request: FraudDetectionRequest): TextAnalysis {
    const { description, productName } = request
    const text = `${productName} ${description}`.toLowerCase()

    // Language quality analysis
    const languageQuality = this.assessLanguageQuality(text)
    const grammarScore = this.assessGrammar(text)

    // Suspicious keywords detection
    const suspiciousKeywords = this.SUSPICIOUS_KEYWORDS.filter((keyword) => text.includes(keyword.toLowerCase()))

    // Claims verification
    const claimsVerification = this.verifyClaims(text)

    // Sentiment analysis (simplified)
    const sentimentScore = this.analyzeSentiment(text)

    return {
      languageQuality,
      grammarScore,
      suspiciousKeywords,
      claimsVerification,
      sentimentScore,
    }
  }

  private static async analyzeCertification(request: FraudDetectionRequest): Promise<CertificationAnalysis> {
    const suspiciousElements: string[] = []

    // Check if product claims to be halal but has no certification
    const claimsHalal =
      request.description.toLowerCase().includes("halal") || request.productName.toLowerCase().includes("halal")

    const hasValidCertification = !!request.certificationImage

    if (claimsHalal && !hasValidCertification) {
      suspiciousElements.push("Claims to be Halal but no certification provided")
    }

    // Simulate certification authority detection
    const certificationAuthority = hasValidCertification ? "JAKIM Malaysia" : "None"

    // Image authenticity for certification
    const imageAuthenticity = hasValidCertification ? Math.random() * 30 + 70 : 0

    // Blockchain verification (simplified)
    const blockchainVerified = hasValidCertification && Math.random() > 0.3

    return {
      hasValidCertification,
      certificationAuthority,
      imageAuthenticity,
      blockchainVerified,
      suspiciousElements,
    }
  }

  private static calculateRiskScore(analyses: {
    priceAnalysis: PriceAnalysis
    sellerAnalysis: SellerAnalysis
    imageAnalysis: ImageAnalysis
    textAnalysis: TextAnalysis
    certificationAnalysis: CertificationAnalysis
  }): number {
    let riskScore = 0

    // Price risk (0-3 points)
    if (analyses.priceAnalysis.priceCategory === "very_low") riskScore += 3
    else if (analyses.priceAnalysis.priceCategory === "low") riskScore += 2
    else if (analyses.priceAnalysis.priceCategory === "very_high") riskScore += 1

    // Seller risk (0-3 points)
    if (analyses.sellerAnalysis.behaviorPattern === "fraudulent") riskScore += 3
    else if (analyses.sellerAnalysis.behaviorPattern === "suspicious") riskScore += 2
    if (analyses.sellerAnalysis.trustScore < 30) riskScore += 1

    // Image risk (0-2 points)
    if (!analyses.imageAnalysis.isAuthentic) riskScore += 2
    if (analyses.imageAnalysis.duplicateDetected) riskScore += 1

    // Text risk (0-2 points)
    if (analyses.textAnalysis.suspiciousKeywords.length > 2) riskScore += 2
    else if (analyses.textAnalysis.suspiciousKeywords.length > 0) riskScore += 1
    if (analyses.textAnalysis.languageQuality < 50) riskScore += 1

    // Certification risk (0-2 points)
    if (analyses.certificationAnalysis.suspiciousElements.length > 0) riskScore += 1
    if (!analyses.certificationAnalysis.hasValidCertification) riskScore += 1

    return Math.min(10, riskScore)
  }

  private static identifyRedFlags(analyses: any): RedFlag[] {
    const redFlags: RedFlag[] = []

    // Price red flags
    if (analyses.priceAnalysis.priceCategory === "very_low") {
      redFlags.push({
        type: "price",
        severity: "high",
        description: "Price significantly below market average",
        evidence: `Price is ${Math.abs(analyses.priceAnalysis.priceDeviation).toFixed(1)}% below market`,
        impact: 4,
      })
    }

    // Seller red flags
    if (analyses.sellerAnalysis.behaviorPattern === "fraudulent") {
      redFlags.push({
        type: "seller",
        severity: "critical",
        description: "Seller profile indicates fraudulent behavior",
        evidence: `Trust score: ${analyses.sellerAnalysis.trustScore}`,
        impact: 5,
      })
    }

    // Image red flags
    if (analyses.imageAnalysis.duplicateDetected) {
      redFlags.push({
        type: "image",
        severity: "high",
        description: "Product images found in other listings",
        evidence: "Duplicate image detection positive",
        impact: 4,
      })
    }

    // Text red flags
    if (analyses.textAnalysis.suspiciousKeywords.length > 0) {
      redFlags.push({
        type: "text",
        severity: "medium",
        description: "Suspicious marketing language detected",
        evidence: `Keywords: ${analyses.textAnalysis.suspiciousKeywords.join(", ")}`,
        impact: 3,
      })
    }

    // Certification red flags
    if (analyses.certificationAnalysis.suspiciousElements.length > 0) {
      redFlags.push({
        type: "certification",
        severity: "high",
        description: "Certification issues detected",
        evidence: analyses.certificationAnalysis.suspiciousElements.join(", "),
        impact: 4,
      })
    }

    return redFlags
  }

  private static determineRiskLevel(riskScore: number): "low" | "medium" | "high" | "critical" {
    if (riskScore >= 8) return "critical"
    if (riskScore >= 6) return "high"
    if (riskScore >= 3) return "medium"
    return "low"
  }

  private static determineAction(
    riskScore: number,
    redFlags: RedFlag[],
  ): "approve" | "flag" | "block" | "manual_review" {
    const criticalFlags = redFlags.filter((flag) => flag.severity === "critical").length
    const highFlags = redFlags.filter((flag) => flag.severity === "high").length

    if (criticalFlags > 0 || riskScore >= 8) return "block"
    if (highFlags > 1 || riskScore >= 6) return "manual_review"
    if (riskScore >= 3) return "flag"
    return "approve"
  }

  private static calculateConfidence(redFlags: RedFlag[]): number {
    const totalImpact = redFlags.reduce((sum, flag) => sum + flag.impact, 0)
    const maxPossibleImpact = 25 // 5 flags * 5 max impact
    return Math.min(100, (totalImpact / maxPossibleImpact) * 100)
  }

  private static calculateFraudProbability(riskScore: number, redFlags: RedFlag[]): number {
    const baseProb = (riskScore / 10) * 60 // 0-60% based on risk score
    const flagBonus = redFlags.length * 5 // 5% per red flag
    return Math.min(100, baseProb + flagBonus)
  }

  private static generateRecommendations(redFlags: RedFlag[], riskLevel: string): string[] {
    const recommendations: string[] = []

    if (riskLevel === "critical" || riskLevel === "high") {
      recommendations.push("Block listing immediately")
      recommendations.push("Investigate seller account")
      recommendations.push("Review similar listings from same seller")
    }

    if (redFlags.some((flag) => flag.type === "price")) {
      recommendations.push("Verify pricing with market analysis")
      recommendations.push("Request price justification from seller")
    }

    if (redFlags.some((flag) => flag.type === "certification")) {
      recommendations.push("Request original certification documents")
      recommendations.push("Verify certification with issuing authority")
    }

    if (redFlags.some((flag) => flag.type === "image")) {
      recommendations.push("Request original product photos")
      recommendations.push("Verify image authenticity")
    }

    if (recommendations.length === 0) {
      recommendations.push("Monitor listing for unusual activity")
      recommendations.push("Regular compliance checks")
    }

    return recommendations
  }

  // Helper methods
  private static estimateMarketPrice(category: string, productName: string): number {
    // Simplified market price estimation
    const basePrices: { [key: string]: number } = {
      food: 15,
      cosmetics: 25,
      supplements: 35,
      clothing: 45,
      electronics: 150,
    }

    return basePrices[category.toLowerCase()] || 20
  }

  private static generateCompetitorPrices(marketPrice: number): number[] {
    const prices: number[] = []
    for (let i = 0; i < 5; i++) {
      const variation = (Math.random() - 0.5) * 0.4 // ±20% variation
      prices.push(marketPrice * (1 + variation))
    }
    return prices
  }

  private static assessLanguageQuality(text: string): number {
    // Simplified language quality assessment
    const wordCount = text.split(" ").length
    const avgWordLength = text.replace(/\s/g, "").length / wordCount

    let score = 70
    if (avgWordLength < 3) score -= 20
    if (wordCount < 10) score -= 15
    if (text.includes("!!!") || text.includes("???")) score -= 10

    return Math.max(0, Math.min(100, score))
  }

  private static assessGrammar(text: string): number {
    // Simplified grammar assessment
    let score = 80

    // Check for common grammar issues
    if (text.includes("  ")) score -= 5 // Double spaces
    if (!/[.!?]$/.test(text.trim())) score -= 10 // No ending punctuation
    if (text.split(".").length < 2) score -= 10 // No sentences

    return Math.max(0, Math.min(100, score))
  }

  private static verifyClaims(text: string): ClaimVerification[] {
    const claims: ClaimVerification[] = []

    // Common claims to verify
    const claimPatterns = [
      { pattern: /100% (halal|authentic|natural|organic)/i, claim: "100% guarantee claim" },
      { pattern: /certified by/i, claim: "Certification claim" },
      { pattern: /award.winning/i, claim: "Award winning claim" },
      { pattern: /doctor recommended/i, claim: "Medical endorsement claim" },
    ]

    claimPatterns.forEach(({ pattern, claim }) => {
      if (pattern.test(text)) {
        claims.push({
          claim,
          isVerifiable: false,
          confidence: 30,
        })
      }
    })

    return claims
  }

  private static analyzeSentiment(text: string): number {
    // Simplified sentiment analysis
    const positiveWords = ["excellent", "amazing", "perfect", "best", "great", "wonderful"]
    const negativeWords = ["bad", "terrible", "awful", "worst", "horrible", "fake"]

    let score = 0
    positiveWords.forEach((word) => {
      if (text.includes(word)) score += 0.1
    })
    negativeWords.forEach((word) => {
      if (text.includes(word)) score -= 0.1
    })

    return Math.max(-1, Math.min(1, score))
  }
}
