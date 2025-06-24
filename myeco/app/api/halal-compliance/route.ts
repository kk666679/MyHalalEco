import { type NextRequest, NextResponse } from "next/server"
import { AuthService } from "@/lib/auth"
import { HalalComplianceAI } from "@/lib/halal-compliance-ai"

export async function POST(request: NextRequest) {
  try {
    // Authenticate request
    const user = await AuthService.authenticateRequest(request)
    if (!user) {
      return NextResponse.json({ success: false, message: "Authentication required" }, { status: 401 })
    }

    const complianceRequest = await request.json()

    // Validate required fields
    if (!complianceRequest.product || !complianceRequest.ingredients) {
      return NextResponse.json(
        { success: false, message: "Product name and ingredients are required" },
        { status: 400 },
      )
    }

    // Perform comprehensive Halal compliance analysis
    const result = await HalalComplianceAI.validateProduct(complianceRequest)

    return NextResponse.json({
      success: true,
      data: result,
      timestamp: new Date().toISOString(),
      requestId: `REQ-${Date.now()}`,
    })
  } catch (error) {
    console.error("Halal compliance analysis error:", error)
    return NextResponse.json(
      {
        success: false,
        message: "Compliance analysis service error",
        error: error instanceof Error ? error.message : "Unknown error",
      },
      { status: 500 },
    )
  }
}

export async function GET(request: NextRequest) {
  try {
    const user = await AuthService.authenticateRequest(request)
    if (!user) {
      return NextResponse.json({ success: false, message: "Authentication required" }, { status: 401 })
    }

    // Return API documentation
    return NextResponse.json({
      success: true,
      documentation: {
        endpoint: "/api/halal-compliance",
        method: "POST",
        description: "Comprehensive Halal compliance analysis for products",
        requiredFields: ["product", "ingredients"],
        optionalFields: [
          "certificationId",
          "supplier",
          "price",
          "sellerRating",
          "certificationImage",
          "category",
          "slaughterMethod",
          "origin",
        ],
        responseFields: [
          "isHalalCompliant",
          "haramIngredients",
          "certificationAuthority",
          "blockchainTxHash",
          "confidenceScore",
          "recommendedAlternatives",
          "complianceDetails",
          "riskAssessment",
        ],
        example: {
          request: {
            product: "Beef Jerky",
            ingredients: ["beef", "salt", "spices", "natural flavors"],
            certificationId: "JAKIM-2023-BJ001",
            category: "meat",
            slaughterMethod: "halal",
            origin: "Malaysia",
          },
          response: {
            isHalalCompliant: true,
            haramIngredients: [],
            certificationAuthority: "JAKIM Malaysia",
            blockchainTxHash: "0x123...",
            confidenceScore: 95,
            recommendedAlternatives: [],
          },
        },
      },
    })
  } catch (error) {
    return NextResponse.json({ success: false, message: "Service error" }, { status: 500 })
  }
}
