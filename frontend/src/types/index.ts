// Auth Types
export interface LoginRequest {
  msisdn: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  msisdn: string;
  role: string;
}

export interface RegisterRequest {
  name: string;
  msisdn: string;
  userType?: 'INDIVIDUAL' | 'CORPORATE';
  currentPlanId?: number;
}

export interface RegisterResponse {
  accessToken: string;
  refreshToken: string;
  msisdn: string;
  role: string;
  message: string;
}

// User Types
export interface User {
  userId: number;
  name: string;
  msisdn: string;
  type: 'INDIVIDUAL' | 'CORPORATE';
  currentPlanId?: number;
  createdAt: string;
  updatedAt: string;
}

// Bill Types - Backend DTO'lara uygun
export interface Bill {
  billId: number;
  userId: number;
  periodStart: string;
  periodEnd: string;
  issueDate: string;
  totalAmount: number;
  currency: string;
  items: BillItem[];
}

export interface BillItem {
  itemId: number;
  billId: number;
  category: string;
  subtype: string;
  description: string;
  amount: number;
  unitPrice: number;
  quantity: number;
  taxRate: number;
  createdAt: string;
}

// Create Bill Types
export interface CreateBillItemRequest {
  category: string;
  subtype: string;
  description: string;
  amount: number;
  unitPrice: number;
  quantity: number;
  taxRate?: number;
}

export interface CreateBillRequest {
  userId: number;
  periodStart: string; // yyyy-MM-dd
  periodEnd: string;   // yyyy-MM-dd
  issueDate: string;   // yyyy-MM-dd
  totalAmount: number;
  currency: string;
  billItems: CreateBillItemRequest[];
}

export interface BillSummary {
  billId: number;
  totalAmount: number;
  taxes: number;
  netAmount: number;
  itemCount: number;
  categoryBreakdown: CategoryBreakdown[];
  usageBasedCharges: number;
  oneTimeCharges: number;
  savingsHint: string;
}

export interface CategoryBreakdown {
  category: string;
  total: number;
  percentage: number;
  explanation?: string;
  lines?: BillItem[];
}

// Catalog Types - Backend DTO'lara uygun
export interface Plan {
  planId: number;
  planName: string;
  planType: string;
  quotaGb: number;
  quotaMin: number;
  quotaSms: number;
  monthlyPrice: number;
  overageGb: number;
  overageMin: number;
  overageSms: number;
}

export interface AddOnPack {
  addonId: number;
  addonName: string;
  addonType: string;
  description: string;
  price: number;
  features: string[];
}

export interface VAS {
  vasId: number;
  vasName: string;
  description: string;
  price: number;
  type: string;
  features: string[];
}

export interface PremiumSMS {
  premiumSmsId: number;
  name: string;
  description: string;
  price: number;
  features: string[];
}

export interface CatalogResponse {
  plans: Plan[];
  addOns: AddOnPack[];
  vas: VAS[];
  premiumSMS: PremiumSMS[];
}

// Anomaly Types - Backend DTO'lara uygun
export interface Anomaly {
  anomalyId: number;
  userId: number;
  billId: number;
  type: string;
  severity: string;
  description: string;
  detectedAt: string;
  status: string;
  zScore: number;
  percentageDifference: number;
  recommendations: string[];
}

export interface AnomalyRequest {
  userId: number;
  period: string;
}

export interface AnomalyResponse {
  anomalies: Anomaly[];
  totalAnomalies: number;
  period: string;
  userId: number;
  anomalySummary: Record<string, number>;
}

// Usage Types - Backend DTO'lara uygun
export interface UsageDaily {
  id: number;
  userId: number;
  date: string;
  mbUsed: number;
  minutesUsed: number;
  smsUsed: number;
  roamingMb: number;
}

export interface UsageSummary {
  userId: number;
  period: string;
  dataUsage: number;
  voiceUsage: number;
  smsUsage: number;
  roamingUsage: number;
  totalCost: number;
  dailyBreakdown: UsageDaily[];
}

// Simulation Types - Backend DTO'lara uygun
export interface SimulationRequest {
  userId: number;
  period: string;
  scenario: {
    planId?: number;
    addons?: number[];
    disableVas?: boolean;
    blockPremiumSms?: boolean;
  };
}

export interface SimulationResponse {
  currentTotal: number;
  newTotal: number;
  saving: number;
  details: {
    planChange: number;
    addOns: number;
    vas: number;
    premiumSms: number;
  };
  recommendations: string[];
}

// Checkout Types - Backend DTO'lara uygun
export interface CheckoutRequest {
  userId: number;
  actions: CheckoutAction[];
}

export interface CheckoutAction {
  type: 'CHANGE_PLAN' | 'ADD_ADDON' | 'CANCEL_VAS' | 'BLOCK_PREMIUM_SMS';
  payload: any;
}

export interface CheckoutResponse {
  success: boolean;
  message: string;
  orderId: string;
  totalCost: number;
}

// Explain Types - Backend DTO'lara uygun
export interface ExplainRequest {
  billId: number;
}

export interface ExplainResponse {
  summary: BillSummary;
  breakdown: CategoryBreakdown[];
  naturalLanguageSummary: string;
}

// Bonus Types - Backend DTO'lara uygun
export interface CohortAnalysis {
  userId: number;
  period: string;
  cohort: string;
  similarUsers: number;
  averageUsage: number;
  recommendations: string[];
}

export interface TaxAnalysis {
  billId: number;
  totalTax: number;
  taxBreakdown: {
    category: string;
    amount: number;
    rate: number;
  }[];
}

export interface AutofixRecommendation {
  autofixId: number;
  userId: number;
  period: string;
  currentCost: number;
  recommendedCost: number;
  savings: number;
  actions: string[];
  confidence: number;
}
