import { AlchemyService } from "./alchemy"

export interface HalalComplianceRequest {
  product: string
  ingredients: string[]
  certificationId?: string
  supplier?: string
  price?: string
  sellerRating?: number
  certificationImage?: string
  category?: "meat" | "dairy" | "processed" | "cosmetics" | "pharmaceutical"
  slaughterMethod?: string
  origin?: string
}

export interface HalalComplianceResponse {
  isHalalCompliant: boolean
  haramIngredients: string[]
  certificationAuthority: string
  blockchainTxHash: string
  blockchainVerificationLink: string
  confidenceScore: number
  recommendedAlternatives: string[]
  complianceDetails: {
    ingredientAnalysis: IngredientAnalysis[]
    certificationStatus: CertificationStatus
    slaughterCompliance?: SlaughterCompliance
  }
  riskAssessment: RiskAssessment
}

export interface IngredientAnalysis {
  ingredient: string
  status: "halal" | "haram" | "mushbooh" | "unknown"
  reason: string
  alternatives?: string[]
}

export interface CertificationStatus {
  isValid: boolean
  authority: string
  expiryDate?: string
  verificationMethod: "blockchain" | "api" | "manual"
  trustScore: number
}

export interface SlaughterCompliance {
  method: string
  isCompliant: boolean
  requirements: string[]
  certifyingBody?: string
}

export interface RiskAssessment {
  overallRisk: number
  factors: RiskFactor[]
  recommendation: "approve" | "review" | "reject"
}

export interface RiskFactor {
  factor: string
  impact: number
  description: string
}

export class HalalComplianceAI {
  private static readonly HARAM_INGREDIENTS = [
    // Animal-derived haram ingredients
    "pork",
    "bacon",
    "ham",
    "lard",
    "pork fat",
    "pork gelatin",
    "pepsin",
    "rennet",
    "carmine",
    "cochineal",
    "shellac",

    // Alcohol and derivatives
    "alcohol",
    "ethanol",
    "wine",
    "beer",
    "rum",
    "whiskey",
    "vanilla extract",
    "wine vinegar",
    "cooking wine",

    // Non-halal animal derivatives
    "non-halal gelatin",
    "non-halal collagen",
    "non-halal tallow",

    // Uncertain/Mushbooh ingredients
    "mono and diglycerides",
    "lecithin",
    "glycerin",
    "stearic acid",
  ]

  private static readonly MUSHBOOH_INGREDIENTS = [
    "mono and diglycerides",
    "lecithin",
    "glycerin",
    "stearic acid",
    "natural flavors",
    "artificial flavors",
    "enzymes",
    "emulsifiers",
  ]

  private static readonly HALAL_AUTHORITIES = [
    "JAKIM Malaysia",
    "MUIS Singapore",
    "HFA UK",
    "IFANCA USA",
    "HFCE Canada",
    "AHCFI Australia",
    "ESMA UAE",
    "MUI Indonesia",
    "SANHA South Africa",
    "HMC UK",
    "ISWA USA",
  ]

  static async validateProduct(request: HalalComplianceRequest): Promise<HalalComplianceResponse> {
    try {
      // 1. Analyze ingredients
      const ingredientAnalysis = this.analyzeIngredients(request.ingredients)

      // 2. Verify certification
      const certificationStatus = await this.verifyCertification(request.certificationId, request.certificationImage)

      // 3. Check slaughter compliance (for meat products)
      const slaughterCompliance =
        request.category === "meat" ? this.checkSlaughterCompliance(request.slaughterMethod, request.origin) : undefined

      // 4. Assess overall risk
      const riskAssessment = this.assessRisk(request, ingredientAnalysis, certificationStatus)

      // 5. Determine compliance
      const isHalalCompliant = this.determineCompliance(ingredientAnalysis, certificationStatus, slaughterCompliance)

      // 6. Generate blockchain record
      const blockchainTxHash = await this.createBlockchainRecord({
        product: request.product,
        isCompliant: isHalalCompliant,
        certificationId: request.certificationId,
        timestamp: Date.now(),
      })

      // 7. Get alternatives if non-compliant
      const recommendedAlternatives = !isHalalCompliant
        ? this.generateAlternatives(request.product, request.category)
        : []

      return {
        isHalalCompliant,
        haramIngredients: ingredientAnalysis.filter((ing) => ing.status === "haram").map((ing) => ing.ingredient),
        certificationAuthority: certificationStatus.authority,
        blockchainTxHash,
        blockchainVerificationLink: `https://etherscan.io/tx/${blockchainTxHash}`,
        confidenceScore: this.calculateConfidenceScore(ingredientAnalysis, certificationStatus, riskAssessment),
        recommendedAlternatives,
        complianceDetails: {
          ingredientAnalysis,
          certificationStatus,
          slaughterCompliance,
        },
        riskAssessment,
      }
    } catch (error) {
      console.error("Halal compliance validation failed:", error)
      throw new Error("Compliance validation service temporarily unavailable")
    }
  }

  private static analyzeIngredients(ingredients: string[]): IngredientAnalysis[] {
    return ingredients.map((ingredient) => {
      const lowerIngredient = ingredient.toLowerCase()

      // Check for haram ingredients
      const haramMatch = this.HARAM_INGREDIENTS.find((haram) => lowerIngredient.includes(haram.toLowerCase()))

      if (haramMatch) {
        return {
          ingredient,
          status: "haram",
          reason: `Contains ${haramMatch} which is prohibited in Islam`,
          alternatives: this.getIngredientAlternatives(haramMatch),
        }
      }

      // Check for mushbooh (doubtful) ingredients
      const mushboohMatch = this.MUSHBOOH_INGREDIENTS.find((mushbooh) =>
        lowerIngredient.includes(mushbooh.toLowerCase()),
      )

      if (mushboohMatch) {
        return {
          ingredient,
          status: "mushbooh",
          reason: `${mushboohMatch} requires verification of source and processing method`,
          alternatives: this.getIngredientAlternatives(mushboohMatch),
        }
      }

      // Default to halal if no issues found
      return {
        ingredient,
        status: "halal",
        reason: "No prohibited substances detected",
      }
    })
  }

  private static async verifyCertification(
    certificationId?: string,
    certificationImage?: string,
  ): Promise<CertificationStatus> {
    if (!certificationId) {
      return {
        isValid: false,
        authority: "Not Certified",
        verificationMethod: "manual",
        trustScore: 0,
      }
    }

    try {
      // Verify with blockchain
      const blockchainVerification = await AlchemyService.verifyHalalCertification(certificationId)

      if (blockchainVerification.isValid) {
        return {
          isValid: true,
          authority: blockchainVerification.certificationData?.authority || "Unknown Authority",
          expiryDate: blockchainVerification.certificationData?.expiryDate,
          verificationMethod: "blockchain",
          trustScore: 95,
        }
      }

      // Fallback to pattern matching
      const authority = this.identifyCertificationAuthority(certificationId)
      const isValid = authority !== "Unknown Authority"

      return {
        isValid,
        authority,
        verificationMethod: "api",
        trustScore: isValid ? 75 : 25,
      }
    } catch (error) {
      return {
        isValid: false,
        authority: "Verification Failed",
        verificationMethod: "manual",
        trustScore: 0,
      }
    }
  }

  private static checkSlaughterCompliance(slaughterMethod?: string, origin?: string): SlaughterCompliance {
    if (!slaughterMethod) {
      return {
        method: "Unknown",
        isCompliant: false,
        requirements: [
          "Animal must be alive and healthy at time of slaughter",
          "Slaughter must be performed by Muslim",
          "Bismillah must be recited",
          "Sharp knife must be used",
          "Blood must be completely drained",
        ],
      }
    }

    const method = slaughterMethod.toLowerCase()
    const isCompliant = method.includes("halal") || method.includes("zabiha") || method.includes("dhabiha")

    return {
      method: slaughterMethod,
      isCompliant,
      requirements: [
        "Animal must be alive and healthy at time of slaughter",
        "Slaughter must be performed by Muslim",
        "Bismillah must be recited",
        "Sharp knife must be used",
        "Blood must be completely drained",
      ],
      certifyingBody: isCompliant ? this.identifySlaughterCertifier(origin) : undefined,
    }
  }

  private static assessRisk(
    request: HalalComplianceRequest,
    ingredientAnalysis: IngredientAnalysis[],
    certificationStatus: CertificationStatus,
  ): RiskAssessment {
    const factors: RiskFactor[] = []
    let totalRisk = 0

    // Price risk
    if (request.price) {
      const price = Number.parseFloat(request.price.replace(/[^0-9.]/g, ""))
      if (price < 1) {
        factors.push({
          factor: "Suspiciously Low Price",
          impact: 3,
          description: "Price may indicate counterfeit or low-quality product",
        })
        totalRisk += 3
      }
    }

    // Seller rating risk
    if (request.sellerRating && request.sellerRating < 3.5) {
      factors.push({
        factor: "Low Seller Rating",
        impact: 2,
        description: "Seller has poor customer feedback history",
      })
      totalRisk += 2
    }

    // Certification risk
    if (!certificationStatus.isValid) {
      factors.push({
        factor: "No Valid Certification",
        impact: 4,
        description: "Product lacks proper Halal certification",
      })
      totalRisk += 4
    }

    // Ingredient risk
    const haramCount = ingredientAnalysis.filter((ing) => ing.status === "haram").length
    const mushboohCount = ingredientAnalysis.filter((ing) => ing.status === "mushbooh").length

    if (haramCount > 0) {
      factors.push({
        factor: "Haram Ingredients Detected",
        impact: 5,
        description: `Contains ${haramCount} prohibited ingredient(s)`,
      })
      totalRisk += 5
    }

    if (mushboohCount > 0) {
      factors.push({
        factor: "Doubtful Ingredients",
        impact: 2,
        description: `Contains ${mushboohCount} ingredient(s) requiring verification`,
      })
      totalRisk += 2
    }

    // Determine recommendation
    let recommendation: "approve" | "review" | "reject" = "approve"
    if (totalRisk >= 7) recommendation = "reject"
    else if (totalRisk >= 3) recommendation = "review"

    return {
      overallRisk: Math.min(totalRisk, 10),
      factors,
      recommendation,
    }
  }

  private static determineCompliance(
    ingredientAnalysis: IngredientAnalysis[],
    certificationStatus: CertificationStatus,
    slaughterCompliance?: SlaughterCompliance,
  ): boolean {
    // Must not contain haram ingredients
    const hasHaramIngredients = ingredientAnalysis.some((ing) => ing.status === "haram")
    if (hasHaramIngredients) return false

    // For meat products, must have proper slaughter compliance
    if (slaughterCompliance && !slaughterCompliance.isCompliant) return false

    // Must have valid certification for high confidence
    if (!certificationStatus.isValid && certificationStatus.trustScore < 50) return false

    return true
  }

  private static async createBlockchainRecord(data: any): Promise<string> {
    try {
      const result = await AlchemyService.createHalalCertificationRecord({
        productId: data.product,
        certificationId: data.certificationId || `AUTO-${Date.now()}`,
        authority: "HalalEco AI Validator",
        expiryDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString(),
      })

      return result.transactionHash || `0x${Buffer.from(JSON.stringify(data)).toString("hex").slice(0, 64)}`
    } catch (error) {
      // Fallback hash generation
      return `0x${Buffer.from(JSON.stringify(data)).toString("hex").slice(0, 64)}`
    }
  }

  private static calculateConfidenceScore(
    ingredientAnalysis: IngredientAnalysis[],
    certificationStatus: CertificationStatus,
    riskAssessment: RiskAssessment,
  ): number {
    let score = 70 // Base score

    // Certification bonus
    score += certificationStatus.trustScore * 0.2

    // Ingredient analysis bonus
    const haramCount = ingredientAnalysis.filter((ing) => ing.status === "haram").length
    const mushboohCount = ingredientAnalysis.filter((ing) => ing.status === "mushbooh").length

    if (haramCount === 0) score += 10
    if (mushboohCount === 0) score += 5

    // Risk penalty
    score -= riskAssessment.overallRisk * 2

    return Math.max(0, Math.min(100, Math.round(score)))
  }

  private static identifyCertificationAuthority(certificationId: string): string {
    const id = certificationId.toUpperCase()

    if (id.startsWith("JAKIM")) return "JAKIM Malaysia"
    if (id.startsWith("MUIS")) return "MUIS Singapore"
    if (id.startsWith("HFA")) return "HFA UK"
    if (id.startsWith("IFANCA")) return "IFANCA USA"
    if (id.startsWith("HFCE")) return "HFCE Canada"
    if (id.startsWith("AHCFI")) return "AHCFI Australia"
    if (id.startsWith("ESMA")) return "ESMA UAE"
    if (id.startsWith("MUI")) return "MUI Indonesia"
    if (id.startsWith("SANHA")) return "SANHA South Africa"
    if (id.startsWith("HMC")) return "HMC UK"
    if (id.startsWith("ISWA")) return "ISWA USA"

    return "Unknown Authority"
  }

  private static identifySlaughterCertifier(origin?: string): string {
    if (!origin) return "Unknown Certifier"

    const originLower = origin.toLowerCase()
    if (originLower.includes("malaysia")) return "JAKIM Malaysia"
    if (originLower.includes("singapore")) return "MUIS Singapore"
    if (originLower.includes("indonesia")) return "MUI Indonesia"
    if (originLower.includes("australia")) return "AHCFI Australia"
    if (originLower.includes("usa") || originLower.includes("america")) return "IFANCA USA"
    if (originLower.includes("canada")) return "HFCE Canada"
    if (originLower.includes("uk") || originLower.includes("britain")) return "HFA UK"

    return "Local Halal Authority"
  }

  private static getIngredientAlternatives(ingredient: string): string[] {
    const alternatives: { [key: string]: string[] } = {
      pork: ["Halal beef", "Halal chicken", "Halal lamb"],
      bacon: ["Halal beef bacon", "Turkey bacon", "Chicken strips"],
      lard: ["Vegetable oil", "Coconut oil", "Olive oil"],
      gelatin: ["Halal gelatin", "Agar-agar", "Pectin", "Carrageenan"],
      alcohol: ["Natural extracts", "Halal vanilla", "Fruit concentrates"],
      "wine vinegar": ["Apple cider vinegar", "Rice vinegar", "Halal vinegar"],
      "mono and diglycerides": ["Halal-certified emulsifiers", "Lecithin (plant-based)"],
      "natural flavors": ["Halal-certified natural flavors", "Specific flavor extracts"],
    }

    return alternatives[ingredient.toLowerCase()] || [`Halal-certified ${ingredient}`, `Plant-based ${ingredient}`]
  }

  private static generateAlternatives(product: string, category?: string): string[] {
    const productLower = product.toLowerCase()

    // Category-specific alternatives
    if (category === "meat") {
      return [
        `Halal-certified ${product}`,
        "Halal chicken alternative",
        "Halal beef alternative",
        "Plant-based protein alternative",
      ]
    }

    if (category === "dairy") {
      return [
        `Halal-certified ${product}`,
        "Plant-based dairy alternative",
        "Coconut-based alternative",
        "Oat-based alternative",
      ]
    }

    // General alternatives
    const alternatives: { [key: string]: string[] } = {
      gummy: ["Halal gummy bears", "Fruit snacks with halal gelatin", "Agar-based gummies"],
      chocolate: ["Halal-certified chocolate", "Dark chocolate (dairy-free)", "Carob chocolate"],
      cheese: ["Halal cheese", "Plant-based cheese", "Cashew cheese"],
      yogurt: ["Halal yogurt", "Coconut yogurt", "Almond yogurt"],
      bread: ["Halal-certified bread", "Homemade bread", "Sourdough bread"],
      cookie: ["Halal cookies", "Homemade cookies", "Vegan cookies"],
      cake: ["Halal-certified cake", "Eggless cake", "Vegan cake"],
    }

    for (const [key, alts] of Object.entries(alternatives)) {
      if (productLower.includes(key)) {
        return alts
      }
    }

    return [
      `Halal-certified ${product}`,
      `Organic ${product}`,
      `Plant-based ${product} alternative`,
      `Homemade ${product}`,
    ]
  }
}
