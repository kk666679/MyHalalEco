import { type NextRequest, NextResponse } from "next/server"
import { api, endpoints } from "@/lib/api-client"

export async function POST(request: NextRequest) {
  try {
    const credentials = await request.json()

    if (!credentials.email || !credentials.password) {
      return NextResponse.json(
        { success: false, message: "Email and password are required" },
        { status: 400 }
      )
    }

    // Call backend API for authentication
    const authResponse = await api.post(endpoints.auth.login, credentials)

    // Create response with the token from backend
    const response = NextResponse.json(authResponse)

    // Set auth token from backend response
    if (authResponse.data?.token) {
      response.cookies.set("auth-token", authResponse.data.token, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "strict",
        maxAge: 7 * 24 * 60 * 60, // 7 days
      })
    }

    return response
  } catch (error) {
    console.error("Login error:", error)
    return NextResponse.json({ success: false, message: "Internal server error" }, { status: 500 })
  }
}
