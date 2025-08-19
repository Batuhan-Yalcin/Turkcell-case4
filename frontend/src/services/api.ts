import axios, { AxiosInstance, AxiosResponse } from 'axios';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  Bill,
  BillSummary,
  CatalogResponse,
  Plan,
  AddOnPack,
  VAS,
  PremiumSMS,
  User,
  Anomaly,
  AnomalyResponse,
  UsageSummary,
  CreateBillRequest,
  SimulationRequest,
  SimulationResponse,
  CheckoutRequest,
  CheckoutResponse,
  ExplainRequest,
  ExplainResponse
} from '../types';

const API_BASE_URL = 'http://localhost:8080/api';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor - JWT token ekleme
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor - Token refresh
    this.api.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response?.status === 401) {
          const refreshToken = localStorage.getItem('refreshToken');
          if (refreshToken) {
            try {
              const response = await this.refreshToken(refreshToken);
              localStorage.setItem('accessToken', response.data.accessToken);
              localStorage.setItem('refreshToken', response.data.refreshToken);
              
              // Retry original request
              error.config.headers.Authorization = `Bearer ${response.data.accessToken}`;
              return this.api.request(error.config);
            } catch (refreshError) {
              localStorage.removeItem('accessToken');
              localStorage.removeItem('refreshToken');
              window.location.href = '/login';
            }
          }
        }
        return Promise.reject(error);
      }
    );
  }

  // Auth Services
  async login(request: LoginRequest): Promise<AxiosResponse<LoginResponse>> {
    return this.api.post('/auth/login', request);
  }

  async register(request: RegisterRequest): Promise<AxiosResponse<RegisterResponse>> {
    return this.api.post('/auth/register', request);
  }

  async refreshToken(refreshToken: string): Promise<AxiosResponse<LoginResponse>> {
    return this.api.post('/auth/refresh', null, {
      headers: { Authorization: `Bearer ${refreshToken}` }
    });
  }

  // User Services
  async getUsers(): Promise<AxiosResponse<{users: User[]}>> {
    return this.api.get('/users');
  }

  async getUserById(id: number): Promise<AxiosResponse<User>> {
    return this.api.get(`/users/${id}`);
  }

  async getUserByMsisdn(msisdn: string): Promise<AxiosResponse<User>> {
    return this.api.get(`/users/msisdn/${msisdn}`);
  }

  // Bill Services
  async getBillById(billId: number): Promise<AxiosResponse<Bill>> {
    return this.api.get(`/bills/${billId}`);
  }

  async getBillByUserIdAndPeriod(userId: number, period: string): Promise<AxiosResponse<Bill>> {
    return this.api.get(`/bills/user/${userId}/period?period=${period}`);
  }

  async getRecentBillsByUserId(userId: number): Promise<AxiosResponse<Bill[]>> {
    return this.api.get(`/bills/${userId}/recent`);
  }

  async getAvailablePeriods(userId: number): Promise<AxiosResponse<string[]>> {
    return this.api.get(`/bills/${userId}/periods`);
  }

  async getBillItemsByBillId(billId: number): Promise<AxiosResponse<any[]>> {
    return this.api.get(`/bills/${billId}/items`);
  }

  async getBillSummary(billId: number): Promise<AxiosResponse<BillSummary>> {
    return this.api.get(`/bills/${billId}/summary`);
  }

  async createBill(request: CreateBillRequest): Promise<AxiosResponse<Bill>> {
    return this.api.post('/bills', request);
  }

  async getBillsByUserIdAndDateRange(
    userId: number,
    startDate: string,
    endDate: string
  ): Promise<AxiosResponse<Bill[]>> {
    return this.api.get(`/bills/${userId}/range?startDate=${startDate}&endDate=${endDate}`);
  }

  // Catalog Services
  async getFullCatalog(): Promise<AxiosResponse<CatalogResponse>> {
    return this.api.get('/catalog');
  }

  async getPlans(): Promise<AxiosResponse<Plan[]>> {
    return this.api.get('/catalog/plans');
  }

  async getAddOns(): Promise<AxiosResponse<AddOnPack[]>> {
    return this.api.get('/catalog/addons');
  }

  async getVAS(): Promise<AxiosResponse<VAS[]>> {
    return this.api.get('/catalog/vas');
  }

  async getPremiumSMS(): Promise<AxiosResponse<PremiumSMS[]>> {
    return this.api.get('/catalog/premium-sms');
  }

  // Anomaly Services
  async getAnomalies(userId: number, period: string): Promise<AxiosResponse<AnomalyResponse>> {
    return this.api.get(`/anomalies?userId=${userId}&period=${period}`);
  }

  async detectAnomalies(request: any): Promise<AxiosResponse<AnomalyResponse>> {
    return this.api.post('/anomalies', request);
  }

  async getAnomalyHistory(userId: number): Promise<AxiosResponse<AnomalyResponse>> {
    return this.api.get(`/anomalies/${userId}/history`);
  }

  async getAnomalySummary(userId: number): Promise<AxiosResponse<AnomalyResponse>> {
    return this.api.get(`/anomalies/${userId}/summary`);
  }

  async createAnomaly(request: any): Promise<AxiosResponse<Anomaly>> {
    return this.api.post('/anomalies', request);
  }

  // Usage Services
  async getUsageSummary(userId: number, period: string): Promise<AxiosResponse<UsageSummary>> {
    return this.api.get(`/usage/${userId}/summary?period=${period}`);
  }

  // Simulation Services
  async simulatePlanChange(request: SimulationRequest): Promise<AxiosResponse<SimulationResponse>> {
    return this.api.post('/whatif/simulate', request);
  }

  async getSimulationScenarios(): Promise<AxiosResponse<any>> {
    return this.api.get('/whatif/1/scenarios');
  }

  async runSimulation(request: SimulationRequest): Promise<AxiosResponse<any>> {
    return this.api.post('/whatif', request);
  }

  // Checkout Services
  async checkout(request: CheckoutRequest): Promise<AxiosResponse<CheckoutResponse>> {
    return this.api.post('/checkout', request);
  }

  async getCartItems(): Promise<AxiosResponse<any>> {
    return this.api.get('/checkout/cart');
  }

  async processCheckout(request: CheckoutRequest): Promise<AxiosResponse<any>> {
    return this.api.post('/checkout', request);
  }

  async getCurrentPlan(userId: number): Promise<AxiosResponse<any>> {
    return this.api.get(`/users/${userId}/current-plan`);
  }

  async getCurrentVAS(userId: number): Promise<AxiosResponse<any>> {
    return this.api.get(`/users/${userId}/current-vas`);
  }

  // Explain Services
  async explainBill(request: ExplainRequest): Promise<AxiosResponse<ExplainResponse>> {
    return this.api.post('/explain', request);
  }

  // Bonus Services
  async getAnomalyExplanation(anomalyId: number, userContext: string): Promise<AxiosResponse<string>> {
    return this.api.post(`/bonus/llm/anomaly?anomalyId=${anomalyId}&userContext=${userContext}`);
  }

  async getCohortAnalysis(userId: number, period: string): Promise<AxiosResponse<any>> {
    return this.api.get(`/bonus/cohort/${userId}?period=${period}`);
  }

  async getTaxAnalysis(billId: number): Promise<AxiosResponse<any>> {
    return this.api.get(`/bonus/tax/${billId}`);
  }

  async getAutofixRecommendations(userId: number, period: string): Promise<AxiosResponse<any>> {
    return this.api.get(`/bonus/autofix/${userId}/best?period=${period}`);
  }
}

export const apiService = new ApiService();
export default apiService;
