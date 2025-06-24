import jwt from "jsonwebtoken"
import type { NextRequest } from "next/server"

export interface JWTPayload {
  userId: string
  email: string
  role: string
  iat?: number
  exp?: number
}

export class AuthService {
  private static readonly JWT_SECRET = process.env.JWT_SECRET!
  private static readonly JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || "7d"

  static generateToken(payload: Omit<JWTPayload, "iat" | "exp">): string {
    return jwt.sign(payload, this.JWT_SECRET, {
      expiresIn: this.JWT_EXPIRES_IN,
    })
  }

  static verifyToken(token: string): JWTPayload | null {
    try {
      return jwt.verify(token, this.JWT_SECRET) as JWTPayload
    } catch (error) {
      console.error("JWT verification failed:", error)
      return null
    }
  }

  static extractTokenFromRequest(request: NextRequest): string | null {
    const authHeader = request.headers.get("authorization")
    if (authHeader && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7)
    }

    // Also check for token in cookies
    const tokenCookie = request.cookies.get("auth-token")
    return tokenCookie?.value || null
  }

  static async authenticateRequest(request: NextRequest): Promise<JWTPayload | null> {
    const token = this.extractTokenFromRequest(request)
    if (!token) return null

    return this.verifyToken(token)
  }
}

export function createAuthResponse(user: any, message = "Authentication successful") {
  const token = AuthService.generateToken({
    userId: user.id,
    email: user.email,
    role: user.role || "user",
  })

  return {
    success: true,
    message,
    token,
    user: {
      id: user.id,
      email: user.email,
      name: user.name,
      role: user.role,
    },
  }
}
