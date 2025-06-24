import { AlchemyService } from "./alchemy"

export interface HalalValidationRequest {
  product: string
  ingredients: string[]
  certificationId?: string
  supplier?: string
  price?: string
  sellerRating?: number
}

export interface HalalValidationResponse {
  isHalalCompliant: boolean
  haramIngredients: string[]
  certificationAuthority: string
  blockchainVerificationLink: string
  confidenceScore: number
  recommendedAlternatives: string[]
  riskScore?: number
  redFlags?: string[]
  recommendedAction?: "allow" | "flag" | "block"
}

export class HalalValidatorService {
  private static readonly HARAM_INGREDIENTS = [
    "pork",
    "bacon",
    "ham",
    "lard",
    "gelatin",
    "alcohol",
    "wine",
    "beer",
    "vanilla extract",
    "rum flavoring",
    "wine vinegar",
    "pepsin",
    "rennet",
    "carmine",
    "cochineal",
    "shellac",
  ]

  private static readonly HALAL_AUTHORITIES = [
    "JAKIM Malaysia",
    "MUIS Singapore",
    "HFA UK",
    "IFANCA USA",
    "HFCE Canada",
    "AHCFI Australia",
    "ESMA UAE",
  ]

  static async validateProduct(request: HalalValidationRequest): Promise<HalalValidationResponse> {
    try {
      // Analyze ingredients for haram substances
      const haramIngredients = this.detectHaramIngredients(request.ingredients)
      const isHalalCompliant = haramIngredients.length === 0

      // Verify certification if provided
      let certificationAuthority = "Not Certified"
      let blockchainVerificationLink = ""

      if (request.certificationId) {
        const verification = await AlchemyService.verifyHalalCertification(request.certificationId)
        if (verification.isValid && verification.certificationData) {
          certificationAuthority = verification.certificationData.authority
          blockchainVerificationLink = `https://etherscan.io/tx/${verification.blockchainRecord?.verificationHash}`
        }
      }

      // Calculate confidence score
      const confidenceScore = this.calculateConfidenceScore(request, isHalalCompliant, !!request.certificationId)

      // Fraud detection analysis
      const fraudAnalysis = this.analyzeFraudRisk(request)

      // Generate recommendations
      const recommendedAlternatives = isHalalCompliant ? [] : this.generateAlternatives(request.product)

      return {
        isHalalCompliant,
        haramIngredients,
        certificationAuthority,
        blockchainVerificationLink,
        confidenceScore,
        recommendedAlternatives,
        riskScore: fraudAnalysis.riskScore,
        redFlags: fraudAnalysis.redFlags,
        recommendedAction: fraudAnalysis.recommendedAction,
      }
    } catch (error) {
      console.error("Halal validation failed:", error)
      throw new Error("Validation service temporarily unavailable")
    }
  }

  private static detectHaramIngredients(ingredients: string[]): string[] {
    return ingredients.filter((ingredient) =>
      this.HARAM_INGREDIENTS.some((haram) => ingredient.toLowerCase().includes(haram.toLowerCase())),
    )
  }

  private static calculateConfidenceScore(
    request: HalalValidationRequest,
    isCompliant: boolean,
    hasCertification: boolean,
  ): number {
    let score = 70 // Base score

    if (hasCertification) score += 20
    if (isCompliant) score += 10
    if (request.supplier && request.supplier.toLowerCase().includes("halal")) score += 5
    if (request.ingredients.length > 0) score += 5

    return Math.min(score, 100)
  }

  private static analyzeFraudRisk(request: HalalValidationRequest): {
    riskScore: number
    redFlags: string[]
    recommendedAction: "allow" | "flag" | "block"
  } {
    const redFlags: string[] = []
    let riskScore = 1

    // Price analysis
    if (request.price) {
      const price = Number.parseFloat(request.price.replace(/[^0-9.]/g, ""))
      if (price < 1) {
        redFlags.push("Suspiciously low price")
        riskScore += 3
      }
    }

    // Seller rating analysis
    if (request.sellerRating && request.sellerRating < 3.5) {
      redFlags.push("Low seller rating")
      riskScore += 2
    }

    // Certification analysis
    if (!request.certificationId) {
      redFlags.push("No certification provided")
      riskScore += 1
    }

    // Determine action
    let recommendedAction: "allow" | "flag" | "block" = "allow"
    if (riskScore >= 6) recommendedAction = "block"
    else if (riskScore >= 3) recommendedAction = "flag"

    return {
      riskScore: Math.min(riskScore, 10),
      redFlags,
      recommendedAction,
    }
  }

  private static generateAlternatives(product: string): string[] {
    const alternatives: { [key: string]: string[] } = {
      "beef sausages": ["Halal beef sausages", "Chicken sausages", "Turkey sausages"],
      gelatin: ["Halal gelatin", "Agar-agar", "Pectin"],
      "vanilla extract": ["Halal vanilla extract", "Vanilla powder", "Natural vanilla"],
    }

    const productLower = product.toLowerCase()
    for (const [key, alts] of Object.entries(alternatives)) {
      if (productLower.includes(key)) {
        return alts
      }
    }

    return [`Halal ${product}`, `Certified ${product}`, `Alternative to ${product}`]
  }
}
