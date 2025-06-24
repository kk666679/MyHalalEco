import { type NextRequest, NextResponse } from "next/server"
import { AuthService } from "@/lib/auth"
import { SupplyChainTracker } from "@/lib/supply-chain-tracker"

export async function GET(request: NextRequest) {
  try {
    // Authenticate request
    const user = await AuthService.authenticateRequest(request)
    if (!user) {
      return NextResponse.json({ success: false, message: "Authentication required" }, { status: 401 })
    }

    // Check if user has analytics access
    if (user.role !== "admin" && user.role !== "analyst") {
      return NextResponse.json({ success: false, message: "Insufficient permissions" }, { status: 403 })
    }

    const { searchParams } = new URL(request.url)
    const startDate = searchParams.get("startDate")
    const endDate = searchParams.get("endDate")

    // Default to last 30 days if no date range provided
    const end = endDate ? new Date(endDate).getTime() : Date.now()
    const start = startDate ? new Date(startDate).getTime() : end - 30 * 24 * 60 * 60 * 1000

    // Generate analytics
    const analytics = await SupplyChainTracker.generateAnalytics({ start, end })

    return NextResponse.json({
      success: true,
      data: analytics,
      dateRange: {
        start: new Date(start).toISOString(),
        end: new Date(end).toISOString(),
      },
      timestamp: new Date().toISOString(),
    })
  } catch (error) {
    console.error("Supply chain analytics error:", error)
    return NextResponse.json(
      {
        success: false,
        message: "Analytics service error",
        error: error instanceof Error ? error.message : "Unknown error",
      },
      { status: 500 },
    )
  }
}

export async function POST(request: NextRequest) {
  try {
    const user = await AuthService.authenticateRequest(request)
    if (!user) {
      return NextResponse.json({ success: false, message: "Authentication required" }, { status: 401 })
    }

    if (user.role !== "admin") {
      return NextResponse.json({ success: false, message: "Admin access required" }, { status: 403 })
    }

    const { action, data } = await request.json()

    switch (action) {
      case "detect_contamination":
        if (!data.recordId || !data.contaminationData) {
          return NextResponse.json(
            { success: false, message: "recordId and contaminationData are required" },
            { status: 400 },
          )
        }

        const alerts = await SupplyChainTracker.detectContamination(data.recordId, data.contaminationData)

        return NextResponse.json({
          success: true,
          data: { alerts },
          message: "Contamination detection completed",
          timestamp: new Date().toISOString(),
        })

      case "create_record":
        if (!data.productId || !data.productName || !data.batchNumber) {
          return NextResponse.json(
            { success: false, message: "productId, productName, and batchNumber are required" },
            { status: 400 },
          )
        }

        const record = await SupplyChainTracker.createSupplyChainRecord(
          data.productId,
          data.productName,
          data.batchNumber,
        )

        return NextResponse.json({
          success: true,
          data: record,
          message: "Supply chain record created",
          timestamp: new Date().toISOString(),
        })

      case "add_stage":
        if (!data.recordId || !data.stageData) {
          return NextResponse.json({ success: false, message: "recordId and stageData are required" }, { status: 400 })
        }

        const updatedRecord = await SupplyChainTracker.addStage(data.recordId, data.stageData)

        return NextResponse.json({
          success: true,
          data: updatedRecord,
          message: "Supply chain stage added",
          timestamp: new Date().toISOString(),
        })

      default:
        return NextResponse.json({ success: false, message: "Invalid action" }, { status: 400 })
    }
  } catch (error) {
    console.error("Supply chain management error:", error)
    return NextResponse.json(
      {
        success: false,
        message: "Supply chain management error",
        error: error instanceof Error ? error.message : "Unknown error",
      },
      { status: 500 },
    )
  }
}
