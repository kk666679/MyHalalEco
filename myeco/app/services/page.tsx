"use client"

import { motion, useScroll, useTransform, AnimatePresence } from "framer-motion"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import {
  ArrowRight,
  Shield,
  BitcoinIcon as Blockchain,
  Search,
  Package,
  TrendingUp,
  DollarSign,
  Globe,
  Grid3X3,
  List,
  Star,
  Users,
} from "lucide-react"
import Link from "next/link"
import { useRef, useState } from "react"

const services = [
  {
    icon: Shield,
    title: "AI-Powered Halal Certification Validator",
    description: "Automated Halal verification for e-commerce listings with blockchain integration",
    longDescription:
      "Our AI analyzes product ingredients, certification IDs, and supplier history to provide comprehensive Halal compliance verification. Returns detailed JSON responses with compliance status, haram ingredients detection, certification authority validation, and blockchain verification links.",
    features: [
      "Ingredient Analysis",
      "Certification Verification",
      "Blockchain Records",
      "Alternative Recommendations",
    ],
    href: "/services/halal-validator",
    color: "bg-emerald-500",
    gradient: "from-emerald-400 to-emerald-600",
    status: "Available",
    rating: 4.9,
    users: "10K+",
    category: "AI Validation",
  },
  {
    icon: Blockchain,
    title: "Blockchain Smart Contract Generator",
    description: "Generate Solidity contracts for transparent Halal certification management",
    longDescription:
      "Create immutable smart contracts for Halal product certification with built-in expiry date checks, revocation functions for regulators, and QR-code-based verification systems.",
    features: ["Immutable Records", "QR Verification", "Expiry Tracking", "Regulatory Controls"],
    href: "/services/smart-contracts",
    color: "bg-blue-500",
    gradient: "from-blue-400 to-blue-600",
    status: "Available",
    rating: 4.8,
    users: "5K+",
    category: "Blockchain",
  },
  {
    icon: Search,
    title: "Fraud Detection AI",
    description: "Prevent fake Halal products with advanced AI fraud detection",
    longDescription:
      "Advanced AI system that analyzes e-commerce product listings to detect potential fraud, providing risk scores, identifying red flags, and recommending appropriate actions.",
    features: ["Risk Scoring", "Seller Verification", "Price Analysis", "Image Authentication"],
    href: "/services/fraud-detection",
    color: "bg-red-500",
    gradient: "from-red-400 to-red-600",
    status: "Beta",
    rating: 4.7,
    users: "2K+",
    category: "AI Security",
  },
  {
    icon: Package,
    title: "Halal NPM Package Auditor",
    description: "Ensure Sharia compliance in software dependencies and packages",
    longDescription:
      "Scan npm packages for Sharia compliance, checking dependencies related to interest (riba), gambling, or unethical AI, along with license restrictions and author reputation analysis.",
    features: ["Dependency Scanning", "License Checking", "Alternative Suggestions", "Reputation Analysis"],
    href: "/services/npm-auditor",
    color: "bg-purple-500",
    gradient: "from-purple-400 to-purple-600",
    status: "Coming Soon",
    rating: 0,
    users: "0",
    category: "Development",
  },
  {
    icon: TrendingUp,
    title: "Personalized Product Recommender",
    description: "AI-driven Halal product recommendations based on user preferences",
    longDescription:
      "Intelligent recommendation system that considers dietary restrictions, budget ranges, and past purchases to suggest the most suitable Halal products with detailed scoring.",
    features: ["Dietary Matching", "Budget Optimization", "Preference Learning", "Halal Scoring"],
    href: "/services/recommender",
    color: "bg-orange-500",
    gradient: "from-orange-400 to-orange-600",
    status: "Available",
    rating: 4.6,
    users: "8K+",
    category: "AI Recommendation",
  },
  {
    icon: Globe,
    title: "Supply Chain Transparency AI",
    description: "End-to-end Halal integrity tracking through supply chain analysis",
    longDescription:
      "Comprehensive supply chain analysis using blockchain data to track ingredient origins, shipping logs, and storage conditions, providing transparency scores and compliance reports.",
    features: ["Origin Tracking", "Storage Monitoring", "Compliance Scoring", "Weakness Detection"],
    href: "/services/supply-chain",
    color: "bg-teal-500",
    gradient: "from-teal-400 to-teal-600",
    status: "Beta",
    rating: 4.5,
    users: "3K+",
    category: "Supply Chain",
  },
  {
    icon: DollarSign,
    title: "Islamic Finance Smart Contracts",
    description: "Sharia-compliant DeFi solutions with profit-sharing mechanisms",
    longDescription:
      "DeFi smart contracts designed for Sharia-compliant profit-sharing (Mudarabah) with no interest (riba), transparent profit distribution, and penalty-free late payments.",
    features: ["Riba-Free Finance", "Profit Sharing", "Transparent Terms", "Risk Disclosure"],
    href: "/services/islamic-finance",
    color: "bg-green-500",
    gradient: "from-green-400 to-green-600",
    status: "Coming Soon",
    rating: 0,
    users: "0",
    category: "Finance",
  },
]

const categories = [
  "All",
  "AI Validation",
  "Blockchain",
  "AI Security",
  "Development",
  "AI Recommendation",
  "Supply Chain",
  "Finance",
]

export default function ServicesPage() {
  const containerRef = useRef<HTMLDivElement>(null)
  const { scrollYProgress } = useScroll({
    target: containerRef,
    offset: ["start start", "end start"],
  })

  const y = useTransform(scrollYProgress, [0, 1], ["0%", "30%"])
  const opacity = useTransform(scrollYProgress, [0, 0.3], [1, 0.8])

  const [searchTerm, setSearchTerm] = useState("")
  const [selectedCategory, setSelectedCategory] = useState("All")
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid")
  const [sortBy, setSortBy] = useState("popular")

  const filteredServices = services
    .filter(
      (service) =>
        service.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        service.description.toLowerCase().includes(searchTerm.toLowerCase()),
    )
    .filter((service) => selectedCategory === "All" || service.category === selectedCategory)
    .sort((a, b) => {
      switch (sortBy) {
        case "rating":
          return b.rating - a.rating
        case "newest":
          return a.status === "Coming Soon" ? -1 : 1
        default:
          return (
            Number.parseInt(b.users.replace(/[^0-9]/g, "") || "0") -
            Number.parseInt(a.users.replace(/[^0-9]/g, "") || "0")
          )
      }
    })

  return (
    <div ref={containerRef} className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-16">
      {/* Hero Section with Parallax */}
      <section className="py-20 bg-gradient-to-r from-emerald-600 to-teal-600 text-white relative overflow-hidden">
        <motion.div style={{ y, opacity }} className="absolute inset-0 bg-black/10" />

        {/* Animated Background Shapes */}
        <motion.div
          animate={{
            rotate: 360,
            scale: [1, 1.2, 1],
          }}
          transition={{
            rotate: { duration: 20, repeat: Number.POSITIVE_INFINITY, ease: "linear" },
            scale: { duration: 4, repeat: Number.POSITIVE_INFINITY, ease: "easeInOut" },
          }}
          className="absolute top-20 right-20 w-32 h-32 bg-white/5 rounded-full blur-xl"
        />

        <div className="container mx-auto px-4 relative z-10">
          <motion.div
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-center max-w-4xl mx-auto"
          >
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.2, type: "spring", stiffness: 200 }}
            >
              <Badge variant="outline" className="mb-4 text-white border-white/30 backdrop-blur-sm">
                Our Services
              </Badge>
            </motion.div>

            <motion.h1
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3, duration: 0.8 }}
              className="text-4xl md:text-6xl font-bold mb-6"
            >
              Comprehensive Halal Solutions
            </motion.h1>

            <motion.p
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.5, duration: 0.8 }}
              className="text-xl md:text-2xl text-white/90 max-w-3xl mx-auto"
            >
              Discover our suite of AI-powered tools designed to ensure Halal compliance across every aspect of your
              business
            </motion.p>
          </motion.div>
        </div>
      </section>

      {/* Filters and Search */}
      <section className="py-8 bg-white border-b">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="flex flex-col lg:flex-row gap-4 items-center justify-between"
          >
            {/* Search */}
            <div className="flex-1 max-w-md">
              <motion.div whileFocus={{ scale: 1.02 }} className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                <Input
                  placeholder="Search services..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </motion.div>
            </div>

            {/* Category Filter */}
            <div className="flex flex-wrap gap-2">
              {categories.map((category) => (
                <motion.button
                  key={category}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  onClick={() => setSelectedCategory(category)}
                  className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
                    selectedCategory === category
                      ? "bg-emerald-500 text-white"
                      : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                  }`}
                >
                  {category}
                </motion.button>
              ))}
            </div>

            {/* View Controls */}
            <div className="flex items-center gap-2">
              <motion.button
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.9 }}
                onClick={() => setViewMode("grid")}
                className={`p-2 rounded ${viewMode === "grid" ? "bg-emerald-500 text-white" : "bg-gray-100"}`}
              >
                <Grid3X3 className="w-4 h-4" />
              </motion.button>
              <motion.button
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.9 }}
                onClick={() => setViewMode("list")}
                className={`p-2 rounded ${viewMode === "list" ? "bg-emerald-500 text-white" : "bg-gray-100"}`}
              >
                <List className="w-4 h-4" />
              </motion.button>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Services Grid */}
      <section className="py-20">
        <div className="container mx-auto px-4">
          <AnimatePresence mode="wait">
            <motion.div
              key={`${selectedCategory}-${viewMode}`}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              transition={{ duration: 0.3 }}
              className={viewMode === "grid" ? "grid md:grid-cols-2 lg:grid-cols-3 gap-8" : "space-y-6"}
            >
              {filteredServices.map((service, index) => (
                <ServiceCard key={service.title} service={service} index={index} viewMode={viewMode} />
              ))}
            </motion.div>
          </AnimatePresence>

          {filteredServices.length === 0 && (
            <motion.div
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              className="text-center py-20"
            >
              <div className="text-6xl mb-4">🔍</div>
              <h3 className="text-2xl font-bold text-gray-900 mb-2">No services found</h3>
              <p className="text-gray-600">Try adjusting your search or filter criteria</p>
            </motion.div>
          )}
        </div>
      </section>
    </div>
  )
}

function ServiceCard({ service, index, viewMode }: { service: any; index: number; viewMode: "grid" | "list" }) {
  const [isHovered, setIsHovered] = useState(false)

  if (viewMode === "list") {
    return (
      <motion.div
        initial={{ opacity: 0, x: -50 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ delay: index * 0.1, duration: 0.5 }}
        whileHover={{ scale: 1.02, x: 10 }}
        onHoverStart={() => setIsHovered(true)}
        onHoverEnd={() => setIsHovered(false)}
        className="bg-white rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 overflow-hidden"
      >
        <div className="flex items-center p-6 gap-6">
          <motion.div
            animate={isHovered ? { rotate: 5, scale: 1.1 } : { rotate: 0, scale: 1 }}
            className={`w-16 h-16 rounded-lg ${service.color} flex items-center justify-center flex-shrink-0`}
          >
            <service.icon className="w-8 h-8 text-white" />
          </motion.div>

          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <h3 className="text-xl font-bold">{service.title}</h3>
              <Badge
                variant={
                  service.status === "Available" ? "default" : service.status === "Beta" ? "secondary" : "outline"
                }
              >
                {service.status}
              </Badge>
            </div>
            <p className="text-gray-600 mb-3">{service.description}</p>

            <div className="flex items-center gap-4 text-sm text-gray-500">
              {service.rating > 0 && (
                <div className="flex items-center gap-1">
                  <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                  <span>{service.rating}</span>
                </div>
              )}
              <div className="flex items-center gap-1">
                <Users className="w-4 h-4" />
                <span>{service.users} users</span>
              </div>
              <Badge variant="outline" className="text-xs">
                {service.category}
              </Badge>
            </div>
          </div>

          <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
            <Button asChild disabled={service.status === "Coming Soon"}>
              <Link href={service.href}>
                {service.status === "Coming Soon" ? "Coming Soon" : "Learn More"}
                {service.status !== "Coming Soon" && <ArrowRight className="ml-2 w-4 h-4" />}
              </Link>
            </Button>
          </motion.div>
        </div>
      </motion.div>
    )
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 50, scale: 0.9 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ delay: index * 0.1, duration: 0.5, type: "spring" }}
      whileHover={{ y: -10, scale: 1.02 }}
      onHoverStart={() => setIsHovered(true)}
      onHoverEnd={() => setIsHovered(false)}
      className="group relative"
    >
      <motion.div
        layout
        className="h-full border-0 shadow-lg hover:shadow-xl transition-all duration-300 bg-white rounded-xl overflow-hidden"
      >
        {/* Gradient Background */}
        <motion.div
          className={`absolute inset-0 bg-gradient-to-r ${service.gradient} opacity-0 group-hover:opacity-5 transition-opacity duration-300`}
        />

        <div className="p-6">
          <div className="flex items-center justify-between mb-4">
            <motion.div
              animate={isHovered ? { rotate: 5, scale: 1.1 } : { rotate: 0, scale: 1 }}
              className={`w-12 h-12 rounded-lg ${service.color} flex items-center justify-center shadow-lg`}
            >
              <service.icon className="w-6 h-6 text-white" />
            </motion.div>
            <Badge
              variant={service.status === "Available" ? "default" : service.status === "Beta" ? "secondary" : "outline"}
            >
              {service.status}
            </Badge>
          </div>

          <h3 className="text-xl font-bold mb-2 group-hover:text-emerald-600 transition-colors">{service.title}</h3>
          <p className="text-gray-600 mb-4 text-sm">{service.description}</p>

          {/* Stats */}
          <div className="flex items-center gap-4 mb-4 text-sm text-gray-500">
            {service.rating > 0 && (
              <div className="flex items-center gap-1">
                <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                <span>{service.rating}</span>
              </div>
            )}
            <div className="flex items-center gap-1">
              <Users className="w-4 h-4" />
              <span>{service.users}</span>
            </div>
          </div>

          {/* Features */}
          <div className="space-y-2 mb-6">
            {service.features.slice(0, 3).map((feature: string, featureIndex: number) => (
              <motion.div
                key={featureIndex}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.3 + featureIndex * 0.1 }}
                className="flex items-center gap-2"
              >
                <div className="w-2 h-2 bg-emerald-500 rounded-full" />
                <span className="text-sm text-gray-600">{feature}</span>
              </motion.div>
            ))}
          </div>

          <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
            <Button asChild className="w-full" disabled={service.status === "Coming Soon"}>
              <Link href={service.href}>
                {service.status === "Coming Soon" ? "Coming Soon" : "Learn More"}
                {service.status !== "Coming Soon" && (
                  <motion.div initial={{ x: 0 }} whileHover={{ x: 5 }} transition={{ type: "spring", stiffness: 400 }}>
                    <ArrowRight className="ml-2 w-4 h-4" />
                  </motion.div>
                )}
              </Link>
            </Button>
          </motion.div>
        </div>
      </motion.div>
    </motion.div>
  )
}
