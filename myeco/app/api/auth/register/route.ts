import { type NextRequest, NextResponse } from "next/server"
import { createAuthResponse } from "@/lib/auth"

export async function POST(request: NextRequest) {
  try {
    const { email, password, name, company, industry } = await request.json()

    if (!email || !password || !name) {
      return NextResponse.json({ success: false, message: "Email, password, and name are required" }, { status: 400 })
    }

    // In a real app, you would:
    // 1. Check if user already exists
    // 2. Hash the password
    // 3. Save to database

    // Mock user creation
    const newUser = {
      id: Date.now().toString(),
      email,
      name,
      company,
      industry,
      role: "user",
      createdAt: new Date().toISOString(),
    }

    // Generate JWT token and return response
    const authResponse = createAuthResponse(newUser, "Registration successful")

    // Set HTTP-only cookie
    const response = NextResponse.json(authResponse)
    response.cookies.set("auth-token", authResponse.token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      maxAge: 7 * 24 * 60 * 60, // 7 days
    })

    return response
  } catch (error) {
    console.error("Registration error:", error)
    return NextResponse.json({ success: false, message: "Internal server error" }, { status: 500 })
  }
}
