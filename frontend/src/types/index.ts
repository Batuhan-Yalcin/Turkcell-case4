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
  id: number;
  name: string;
  msisdn: string;
  type: 'INDIVIDUAL' | 'CORPORATE';
  currentPlanId?: number;
  createdAt: string;
  updatedAt: string;
}

// Bill Types
export interface Bill {
  id: number;
  userId: number;
  period: string;
  totalAmount: number;
  taxAmount: number;
  netAmount: number;
  dueDate: string;
  status: string;
  items: BillItem[];
  createdAt: string;
  updatedAt: string;
}

export interface BillItem {
  id: number;
  billId: number;
  category: string;
  description: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  taxRate: number;
  taxAmount: number;
}

export interface BillSummary {
  billId: number;
  totalAmount: number;
  taxAmount: number;
  netAmount: number;
  itemCount: number;
  categoryBreakdown: CategoryBreakdown[];
}

export interface CategoryBreakdown {
  category: string;
  amount: number;
  percentage: number;
}

// Catalog Types
export interface Plan {
  id: number;
  name: string;
  description: string;
  price: number;
  dataLimit: string;
  voiceLimit: string;
  smsLimit: string;
  type: 'PREPAID' | 'POSTPAID';
  features: string[];
}

export interface AddOnPack {
  id: number;
  name: string;
  description: string;
  price: number;
  type: 'DATA' | 'VOICE' | 'SMS' | 'VALUE';
  features: string[];
}

export interface VAS {
  id: number;
  name: string;
  description: string;
  price: number;
  type: string;
  features: string[];
}

export interface PremiumSMS {
  id: number;
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

// Anomaly Types
export interface Anomaly {
  id: number;
  userId: number;
  billId: number;
  type: 'UNUSUAL_USAGE' | 'PRICE_SPIKE' | 'UNEXPECTED_CHARGE';
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  description: string;
  detectedAt: string;
  status: 'DETECTED' | 'INVESTIGATING' | 'RESOLVED';
}

export interface AnomalyRequest {
  userId: number;
  billId: number;
  type: string;
  description: string;
}

// Usage Types
export interface UsageDaily {
  id: number;
  userId: number;
  date: string;
  dataUsage: number;
  voiceUsage: number;
  smsUsage: number;
  totalCost: number;
}

export interface UsageSummary {
  userId: number;
  period: string;
  totalDataUsage: number;
  totalVoiceUsage: number;
  totalSmsUsage: number;
  totalCost: number;
  dailyBreakdown: UsageDaily[];
}

// Simulation Types
export interface SimulationRequest {
  userId: number;
  currentPlanId: number;
  newPlanId: number;
  addOnIds: number[];
  vasIds: number[];
}

export interface SimulationResponse {
  currentCost: number;
  newCost: number;
  savings: number;
  savingsPercentage: number;
  details: {
    planChange: number;
    addOns: number;
    vas: number;
  };
}

// Checkout Types
export interface CheckoutRequest {
  userId: number;
  planId: number;
  addOnIds: number[];
  vasIds: number[];
  action: 'ACTIVATE' | 'MODIFY' | 'CANCEL';
}

export interface CheckoutResponse {
  success: boolean;
  message: string;
  orderId?: string;
  totalCost?: number;
}

// Explain Types
export interface ExplainRequest {
  billId: number;
  question: string;
}

export interface ExplainResponse {
  explanation: string;
  confidence: number;
  relatedItems: string[];
}
