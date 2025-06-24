import { type NextRequest, NextResponse } from "next/server"
import { AuthService } from "@/lib/auth"
import { FraudDetectionAI } from "@/lib/fraud-detection-ai"

export async function POST(request: NextRequest) {
  try {
    // Authenticate request
    const user = await AuthService.authenticateRequest(request)
    if (!user) {
      return NextResponse.json({ success: false, message: "Authentication required" }, { status: 401 })
    }

    const fraudRequest = await request.json()

    // Validate required fields
    const requiredFields = ["productId", "productName", "price", "sellerRating", "sellerHistory"]
    for (const field of requiredFields) {
      if (!fraudRequest[field]) {
        return NextResponse.json({ success: false, message: `${field} is required` }, { status: 400 })
      }
    }

    // Perform fraud detection analysis
    const result = await FraudDetectionAI.analyzeForFraud(fraudRequest)

    // Log high-risk detections
    if (result.riskLevel === "high" || result.riskLevel === "critical") {
      console.warn("High-risk fraud detection:", {
        productId: fraudRequest.productId,
        riskScore: result.riskScore,
        redFlags: result.redFlags.length,
        action: result.recommendedAction,
      })
    }

    return NextResponse.json({
      success: true,
      data: result,
      timestamp: new Date().toISOString(),
      requestId: `FRAUD-${Date.now()}`,
    })
  } catch (error) {
    console.error("Fraud detection error:", error)
    return NextResponse.json(
      {
        success: false,
        message: "Fraud detection service error",
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

    return NextResponse.json({
      success: true,
      documentation: {
        endpoint: "/api/fraud-detection",
        method: "POST",
        description: "AI-powered fraud detection for Halal e-commerce listings",
        requiredFields: ["productId", "productName", "price", "sellerRating", "sellerHistory"],
        optionalFields: [
          "certificationImage",
          "ingredients",
          "productImages",
          "description",
          "category",
          "supplier",
          "location",
        ],
        responseFields: [
          "riskScore",
          "riskLevel",
          "redFlags",
          "recommendedAction",
          "confidence",
          "fraudProbability",
          "detailedAnalysis",
          "recommendations",
        ],
        riskLevels: {
          low: "0-2 risk score",
          medium: "3-5 risk score",
          high: "6-7 risk score",
          critical: "8-10 risk score",
        },
        actions: {
          approve: "Product can be listed",
          flag: "Product needs monitoring",
          block: "Product should be blocked",
          manual_review: "Requires human review",
        },
      },
    })
  } catch (error) {
    return NextResponse.json({ success: false, message: "Service error" }, { status: 500 })
  }
}
