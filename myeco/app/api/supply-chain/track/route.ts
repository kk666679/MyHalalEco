import { type NextRequest, NextResponse } from "next/server"
import { AuthService } from "@/lib/auth"
import { SupplyChainTracker } from "@/lib/supply-chain-tracker"

export async function POST(request: NextRequest) {
  try {
    // Authenticate request
    const user = await AuthService.authenticateRequest(request)
    if (!user) {
      return NextResponse.json({ success: false, message: "Authentication required" }, { status: 401 })
    }

    const trackingQuery = await request.json()

    // Validate that at least one tracking parameter is provided
    if (
      !trackingQuery.productId &&
      !trackingQuery.batchNumber &&
      !trackingQuery.qrCode &&
      !trackingQuery.blockchainHash
    ) {
      return NextResponse.json(
        { success: false, message: "At least one tracking parameter is required" },
        { status: 400 },
      )
    }

    // Track the product
    const result = await SupplyChainTracker.trackProduct(trackingQuery)

    if (!result) {
      return NextResponse.json({ success: false, message: "Product not found in supply chain" }, { status: 404 })
    }

    return NextResponse.json({
      success: true,
      data: result,
      timestamp: new Date().toISOString(),
      requestId: `TRACK-${Date.now()}`,
    })
  } catch (error) {
    console.error("Supply chain tracking error:", error)
    return NextResponse.json(
      {
        success: false,
        message: "Supply chain tracking service error",
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

    // Get query parameters for tracking
    const { searchParams } = new URL(request.url)
    const productId = searchParams.get("productId")
    const batchNumber = searchParams.get("batchNumber")
    const qrCode = searchParams.get("qrCode")
    const blockchainHash = searchParams.get("blockchainHash")

    if (!productId && !batchNumber && !qrCode && !blockchainHash) {
      return NextResponse.json({
        success: true,
        documentation: {
          endpoint: "/api/supply-chain/track",
          methods: ["GET", "POST"],
          description: "Track products through the Halal supply chain",
          queryParameters: ["productId", "batchNumber", "qrCode", "blockchainHash"],
          responseFields: [
            "productId",
            "productName",
            "batchNumber",
            "stages",
            "currentStage",
            "overallCompliance",
            "riskScore",
            "alerts",
          ],
          stageTypes: ["sourcing", "processing", "packaging", "distribution", "retail"],
        },
      })
    }

    // Track using query parameters
    const result = await SupplyChainTracker.trackProduct({
      productId: productId || undefined,
      batchNumber: batchNumber || undefined,
      qrCode: qrCode || undefined,
      blockchainHash: blockchainHash || undefined,
    })

    if (!result) {
      return NextResponse.json({ success: false, message: "Product not found" }, { status: 404 })
    }

    return NextResponse.json({
      success: true,
      data: result,
      timestamp: new Date().toISOString(),
    })
  } catch (error) {
    return NextResponse.json({ success: false, message: "Service error" }, { status: 500 })
  }
}
