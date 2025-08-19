import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  Avatar,
  IconButton,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Alert,
  Divider,
  useTheme,
  Fade,
  Grow,
  LinearProgress,
  Badge,
  Stack,
} from '@mui/material';

import {
  Phone,
  Wifi,
  Sms,
  Public,
  TrendingUp,
  TrendingDown,
  Warning,
  CheckCircle,
  Info,
  Refresh,
  Download,
  Visibility,
  AccountBalance,
  Receipt,
  Analytics,
  CompareArrows,
  ShoppingCart,
  Star,
  Business,
  Person,
  Speed,
  Security,
  Support,
  SmartToy,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import apiService from '../../services/api';
import { Bill, BillSummary, CatalogResponse, Plan, AddOnPack, VAS, PremiumSMS, Anomaly, UsageSummary } from '../../types';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`dashboard-tabpanel-${index}`}
      aria-labelledby={`dashboard-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();
  const [tabValue, setTabValue] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  
  // State for different data
  const [recentBills, setRecentBills] = useState<Bill[]>([]);
  const [catalog, setCatalog] = useState<CatalogResponse | null>(null);
  const [anomalies, setAnomalies] = useState<Anomaly[]>([]);
  const [usageSummary, setUsageSummary] = useState<UsageSummary | null>(null);
  const [selectedPeriod, setSelectedPeriod] = useState('2025-02');

  useEffect(() => {
    loadDashboardData();
    
    // Auto-refresh every 5 minutes
    const interval = setInterval(() => {
      loadDashboardData();
    }, 5 * 60 * 1000);
    
    return () => clearInterval(interval);
  }, []);

  const loadDashboardData = async () => {
    try {
      setIsLoading(true);
      setError('');
      
      // Load catalog data
      const catalogResponse = await apiService.getFullCatalog();
      setCatalog(catalogResponse.data);
      
      // Load recent bills - use current user ID or fallback to 1
      const userId = user?.userId || 1;
      try {
        const billsResponse = await apiService.getRecentBillsByUserId(userId);
        setRecentBills(billsResponse.data);
        console.log('Loaded bills:', billsResponse.data);
      } catch (err) {
        console.log('Bills API error:', err);
        // Try to get bills by period
        try {
          const billsByPeriod = await apiService.getBillsByUserIdAndDateRange(
            userId, 
            '2025-01-01', 
            '2025-12-31'
          );
          setRecentBills(billsByPeriod.data);
          console.log('Loaded bills by period:', billsByPeriod.data);
        } catch (periodErr) {
          console.log('Bills by period API error:', periodErr);
          setRecentBills([]);
        }
      }
      
      // Load anomalies data
      try {
        const anomaliesResponse = await apiService.getAnomalies(userId, selectedPeriod);
        console.log('Anomalies response:', anomaliesResponse.data);
        if (anomaliesResponse.data && anomaliesResponse.data.anomalies) {
          setAnomalies(anomaliesResponse.data.anomalies);
        } else if (anomaliesResponse.data && Array.isArray(anomaliesResponse.data)) {
          setAnomalies(anomaliesResponse.data);
        } else {
          setAnomalies([]);
        }
      } catch (err) {
        console.log('Anomalies API error:', err);
        // Try to get anomaly history
        try {
          const anomalyHistory = await apiService.getAnomalyHistory(userId);
          if (anomalyHistory.data && anomalyHistory.data.anomalies) {
            setAnomalies(anomalyHistory.data.anomalies);
          } else if (anomalyHistory.data && Array.isArray(anomalyHistory.data)) {
            setAnomalies(anomalyHistory.data);
          } else {
            setAnomalies([]);
          }
        } catch (historyErr) {
          console.log('Anomaly history API error:', historyErr);
          setAnomalies([]);
        }
      }
      
      // Load usage summary
      try {
        const usageResponse = await apiService.getUsageSummary(userId, selectedPeriod);
        setUsageSummary(usageResponse.data);
        console.log('Usage summary:', usageResponse.data);
      } catch (err) {
        console.log('Usage API error:', err);
        // Fallback to mock data
        const mockUsageSummary = {
          userId: userId,
          period: selectedPeriod,
          dataUsage: 4.2,
          voiceUsage: 45,
          smsUsage: 12,
          roamingUsage: 0.5,
          totalCost: 89.99,
          dailyBreakdown: []
        };
        setUsageSummary(mockUsageSummary);
      }
      
    } catch (err: any) {
      console.error('Dashboard data loading error:', err);
      setError(err.response?.data?.message || 'Dashboard verileri yüklenirken bir hata oluştu');
    } finally {
      setIsLoading(false);
    }
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const getStatusColor = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'active':
      case 'success':
        return 'success';
      case 'warning':
        return 'warning';
      case 'error':
      case 'failed':
        return 'error';
      default:
        return 'default';
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ 
        display: 'flex', 
        flexDirection: 'column',
        justifyContent: 'center', 
        alignItems: 'center', 
        minHeight: '60vh',
        background: 'linear-gradient(135deg, #F8F9FA 0%, #E3F2FD 100%)'
      }}>
        <CircularProgress size={80} sx={{ color: '#00A3E0', mb: 3 }} />
        <Typography variant="h6" sx={{ color: '#0077A3', fontWeight: 500 }}>
          Dashboard yükleniyor...
        </Typography>
        <Typography variant="body2" sx={{ color: '#0077A3', mt: 1, opacity: 0.7 }}>
          Veriler hazırlanıyor
        </Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ 
      minHeight: '100vh', 
      background: 'linear-gradient(135deg, #F8F9FA 0%, #E3F2FD 100%)',
      py: 4,
      position: 'relative',
      '&::before': {
        content: '""',
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'radial-gradient(circle at 20% 80%, rgba(0, 163, 224, 0.05) 0%, transparent 50%), radial-gradient(circle at 80% 20%, rgba(255, 215, 0, 0.05) 0%, transparent 50%)',
        pointerEvents: 'none',
        zIndex: 0
      }
    }}>
      <Container maxWidth="xl" sx={{ position: 'relative', zIndex: 1 }}>
        {/* Header */}
        <Grow in={true} timeout={800}>
          <Paper
            elevation={0}
            sx={{
              p: 4,
              mb: 3,
              borderRadius: 4,
              background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
              color: 'white',
              position: 'relative',
              overflow: 'hidden',
              boxShadow: '0 8px 32px rgba(0, 163, 224, 0.3)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
            }}
          >
            <Box sx={{ position: 'relative', zIndex: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <Avatar
                    sx={{
                      bgcolor: 'rgba(255, 255, 255, 0.2)',
                      mr: 2,
                      width: 60,
                      height: 60,
                    }}
                  >
                    {user?.role === 'ROLE_ADMIN' ? <Business /> : <Person />}
                  </Avatar>
                  <Box>
                    <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
                      Hoş Geldiniz, {user?.msisdn || 'Kullanıcı'}
                    </Typography>
                    <Typography variant="h6" sx={{ opacity: 0.9, fontWeight: 500 }}>
                      {user?.role === 'ROLE_ADMIN' ? 'Kurumsal Hesap' : 'Bireysel Hesap'}
                    </Typography>
                  </Box>
                </Box>
                
                {/* Sağ üst bilgiler */}
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 1 }}>
                  <Typography variant="h6" sx={{ fontWeight: 600, color: '#FFD700' }}>
                    {user?.msisdn || 'N/A'}
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Dönem: {selectedPeriod}
                  </Typography>
                </Box>
              </Box>
              
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <Chip
                  icon={<Phone />}
                  label={`MSISDN: ${user?.msisdn || 'N/A'}`}
                  sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', color: 'white' }}
                />
                <Chip
                  icon={<AccountBalance />}
                  label={`Rol: ${user?.role === 'ROLE_ADMIN' ? 'Admin' : 'Kullanıcı'}`}
                  sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', color: 'white' }}
                />
                <Chip
                  icon={<Receipt />}
                  label={`Dönem: ${selectedPeriod}`}
                  sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', color: 'white' }}
                />
              </Box>
            </Box>
          </Paper>
        </Grow>

        {/* Error Alert */}
        {error && (
          <Grow in={true} timeout={500}>
            <Alert 
              severity="error" 
              sx={{ 
                mb: 3, 
                borderRadius: 3,
                boxShadow: '0 4px 20px rgba(244, 67, 54, 0.2)',
                border: '1px solid rgba(244, 67, 54, 0.1)'
              }}
              action={
                <Button color="inherit" size="small" onClick={() => setError('')}>
                  Kapat
                </Button>
              }
            >
              {error}
            </Alert>
          </Grow>
        )}

        {/* Quick Stats */}
        {isLoading ? (
          <Box sx={{ 
            display: 'grid', 
            gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' },
            gap: 3, 
            mb: 4 
          }}>
            {[1, 2, 3, 4].map((index) => (
              <Box key={index}>
                <Card sx={{ borderRadius: 3, boxShadow: 3 }}>
                  <CardContent sx={{ textAlign: 'center', p: 3 }}>
                    <CircularProgress size={40} />
                    <Typography variant="body2" sx={{ mt: 2, color: 'text.secondary' }}>
                      Yükleniyor...
                    </Typography>
                  </CardContent>
                </Card>
              </Box>
            ))}
          </Box>
        ) : (
          <Box sx={{ 
            display: 'grid', 
            gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' },
            gap: 3, 
            mb: 4 
          }}>
          <Box>
            <Grow in={true} timeout={1000}>
              <Card sx={{ 
                borderRadius: 3, 
                boxShadow: 3,
                background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                color: 'white',
                '&:hover': {
                  transform: 'translateY(-6px)',
                  boxShadow: '0 12px 40px rgba(0, 163, 224, 0.4)',
                },
                transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
              }}>
                <CardContent sx={{ textAlign: 'center', p: 3 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                    <Receipt sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    {recentBills.length}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Toplam Fatura
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>
                    ₺{recentBills.reduce((sum, bill) => sum + (bill.totalAmount || 0), 0).toFixed(2)}
                  </Typography>
                </CardContent>
              </Card>
            </Grow>
          </Box>
          
          <Box>
            <Grow in={true} timeout={1200}>
              <Card sx={{ 
                borderRadius: 3, 
                boxShadow: 3,
                background: 'linear-gradient(135deg, #E60000 0%, #CC0000 100%)',
                color: 'white',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 6,
                },
                transition: 'all 0.3s ease'
              }}>
                <CardContent sx={{ textAlign: 'center', p: 3 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                    <Warning sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    {anomalies.length}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Anomali Tespit
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>
                    {anomalies.filter(a => a.severity === 'HIGH').length} Kritik
                  </Typography>
                </CardContent>
              </Card>
            </Grow>
          </Box>
          
          <Box>
            <Grow in={true} timeout={1400}>
              <Card sx={{ 
                borderRadius: 3, 
                boxShadow: 3,
                background: 'linear-gradient(135deg, #00C851 0%, #00994A 100%)',
                color: 'white',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 6,
                },
                transition: 'all 0.3s ease'
              }}>
                <CardContent sx={{ textAlign: 'center', p: 3 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                    <TrendingUp sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    {catalog?.plans?.length || 0}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Aktif Plan
                  </Typography>
                </CardContent>
              </Card>
            </Grow>
          </Box>
          
          <Box>
            <Grow in={true} timeout={1600}>
              <Card sx={{ 
                borderRadius: 3, 
                boxShadow: 3,
                background: 'linear-gradient(135deg, #FF9800 0%, #F57C00 100%)',
                color: 'white',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 6,
                },
                transition: 'all 0.3s ease'
              }}>
                <CardContent sx={{ textAlign: 'center', p: 3 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                    <Analytics sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    {usageSummary?.dataUsage?.toFixed(1) || '0'} GB
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Data Kullanımı
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.7)' }}>
                    {usageSummary?.voiceUsage || 0} dk Ses
                  </Typography>
                </CardContent>
              </Card>
            </Grow>
          </Box>
        </Box>
        )}

        {/* Main Content Tabs */}
        <Paper sx={{ 
          borderRadius: 4, 
          boxShadow: '0 4px 20px rgba(0, 0, 0, 0.1)', 
          overflow: 'hidden',
          border: '1px solid rgba(0, 163, 224, 0.1)'
        }}>
          <Box sx={{ 
            borderBottom: 1, 
            borderColor: 'divider',
            background: 'linear-gradient(90deg, #00A3E0 0%, #0077A3 100%)',
            boxShadow: '0 2px 8px rgba(0, 163, 224, 0.2)'
          }}>
            <Tabs
              value={tabValue}
              onChange={handleTabChange}
              aria-label="dashboard tabs"
              sx={{
                '& .MuiTab-root': {
                  minHeight: 64,
                  fontSize: '1rem',
                  fontWeight: 600,
                  color: 'rgba(255, 255, 255, 0.9)',
                  '&.Mui-selected': {
                    color: 'white',
                    fontWeight: 700,
                  },
                  '&:hover': {
                    color: 'white',
                    opacity: 1,
                  },
                },
                '& .MuiTabs-indicator': {
                  backgroundColor: '#FFD700',
                  height: 4,
                },
              }}
            >
              <Tab label="Faturalar" icon={<Receipt />} iconPosition="start" />
              <Tab label="Katalog" icon={<ShoppingCart />} iconPosition="start" />
              <Tab label="Anomaliler" icon={<Warning />} iconPosition="start" />
              <Tab label="AI Analiz" icon={<SmartToy />} iconPosition="start" />
              <Tab label="Kullanım" icon={<Analytics />} iconPosition="start" />
              <Tab label="Simülasyon" icon={<CompareArrows />} iconPosition="start" />
            </Tabs>
          </Box>

          {/* Faturalar Tab */}
          <TabPanel value={tabValue} index={0}>
            <Box sx={{ mb: 3 }}>
              <Typography variant="h5" sx={{ fontWeight: 700, mb: 2, color: 'primary.main' }}>
                Son Faturalar
              </Typography>
              {recentBills.length > 0 ? (
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Fatura ID</TableCell>
                        <TableCell>Dönem</TableCell>
                        <TableCell>Toplam Tutar</TableCell>
                        <TableCell>Durum</TableCell>
                        <TableCell>İşlemler</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {recentBills.map((bill) => (
                        <TableRow key={bill.billId}>
                          <TableCell>{bill.billId}</TableCell>
                          <TableCell>
                            {bill.periodStart && bill.periodEnd ? 
                              `${bill.periodStart} - ${bill.periodEnd}` : 'N/A'
                            }
                          </TableCell>
                          <TableCell>
                            <Typography variant="h6" sx={{ fontWeight: 600, color: 'primary.main' }}>
                              ₺{bill.totalAmount || 0}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Chip
                              label="Ödendi"
                              color="success"
                              size="small"
                              icon={<CheckCircle />}
                            />
                          </TableCell>
                          <TableCell>
                            <Stack direction="row" spacing={1}>
                              <Button
                                size="small"
                                startIcon={<Visibility />}
                                variant="outlined"
                              >
                                Görüntüle
                              </Button>
                              <Button
                                size="small"
                                startIcon={<Download />}
                                variant="outlined"
                              >
                                İndir
                              </Button>
                              <Button
                                size="small"
                                variant="contained"
                                onClick={() => navigate('/bills')}
                                sx={{
                                  background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                                  '&:hover': {
                                    background: 'linear-gradient(135deg, #0077A3 0%, #005580 100%)',
                                  },
                                }}
                              >
                                Detaylar
                              </Button>
                            </Stack>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Alert severity="info">Henüz fatura bulunmuyor.</Alert>
              )}
              
              <Box sx={{ mt: 3, textAlign: 'center' }}>
                <Button
                  variant="contained"
                  size="large"
                  onClick={() => navigate('/bills')}
                  startIcon={<Receipt />}
                  sx={{
                    borderRadius: 3,
                    px: 4,
                    py: 1.5,
                    background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                    '&:hover': {
                      background: 'linear-gradient(135deg, #0077A3 0%, #005580 100%)',
                    },
                  }}
                >
                  Tüm Faturaları Görüntüle
                </Button>
              </Box>
            </Box>
          </TabPanel>

          {/* Katalog Tab */}
          <TabPanel value={tabValue} index={1}>
            <Typography variant="h5" sx={{ fontWeight: 700, mb: 3, color: 'primary.main' }}>
              Katalog
            </Typography>
            <Box sx={{ 
              display: 'grid', 
              gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
              gap: 3 
            }}>
              {/* Plans */}
              <Box>
                <Card sx={{ borderRadius: 3, height: '100%' }}>
                  <CardContent>
                    <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, display: 'flex', alignItems: 'center' }}>
                      <Phone sx={{ mr: 1, color: 'primary.main' }} />
                      Tarife Planları
                    </Typography>
                    {catalog?.plans?.map((plan: Plan) => (
                      <Box key={plan.planId} sx={{ mb: 2, p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
                        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                          {plan.planName}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {plan.quotaGb}GB Data • {plan.quotaMin} Dakika • {plan.quotaSms} SMS
                        </Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main', mt: 1 }}>
                          ₺{plan.monthlyPrice}/ay
                        </Typography>
                      </Box>
                    ))}
                  </CardContent>
                </Card>
              </Box>

              {/* Add-ons */}
              <Box>
                <Card sx={{ borderRadius: 3, height: '100%' }}>
                  <CardContent>
                    <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, display: 'flex', alignItems: 'center' }}>
                      <Star sx={{ mr: 1, color: 'warning.main' }} />
                      Ek Paketler
                    </Typography>
                    {catalog?.addOns?.map((addon: AddOnPack) => (
                      <Box key={addon.addonId} sx={{ mb: 2, p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
                        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                          {addon.addonName}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {addon.description}
                        </Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700, color: 'warning.main', mt: 1 }}>
                          ₺{addon.price}/ay
                        </Typography>
                      </Box>
                    ))}
                  </CardContent>
                </Card>
              </Box>
            </Box>
          </TabPanel>

          {/* Anomaliler Tab */}
          <TabPanel value={tabValue} index={2}>
            <Typography variant="h5" sx={{ fontWeight: 700, mb: 3, color: 'primary.main' }}>
              Anomali Tespitleri
            </Typography>
            {anomalies.length > 0 ? (
              <Box sx={{ 
                display: 'grid', 
                gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
                gap: 3 
              }}>
                {anomalies.map((anomaly, index) => (
                  <Box key={index}>
                    <Card sx={{ 
                      borderRadius: 3, 
                      border: '2px solid', 
                      borderColor: anomaly.severity === 'HIGH' ? 'error.main' : 'warning.main',
                      background: anomaly.severity === 'HIGH' ? 'linear-gradient(135deg, #FFEBEE 0%, #FFCDD2 100%)' : 'linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%)',
                      '&:hover': {
                        transform: 'translateY(-4px)',
                        boxShadow: 6,
                      },
                      transition: 'all 0.3s ease'
                    }}>
                      <CardContent>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                          <Warning sx={{ 
                            color: anomaly.severity === 'HIGH' ? 'error.main' : 'warning.main', 
                            mr: 1,
                            fontSize: 28
                          }} />
                          <Typography variant="h6" sx={{ fontWeight: 700 }}>
                            Anomali #{index + 1}
                          </Typography>
                        </Box>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                          {anomaly.description || 'Anomali tespit edildi'}
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                          <Chip
                            label={anomaly.type || 'DATA'}
                            color="warning"
                            size="small"
                            sx={{ fontWeight: 600 }}
                          />
                          <Chip
                            label={`${anomaly.severity || 'MEDIUM'}`}
                            color={anomaly.severity === 'HIGH' ? 'error' : 'warning'}
                            size="small"
                            sx={{ fontWeight: 600 }}
                          />
                        </Box>
                      </CardContent>
                    </Card>
                  </Box>
                ))}
              </Box>
            ) : (
              <Alert severity="success" icon={<CheckCircle />}>
                Anomali tespit edilmedi. Faturalarınız normal seyrediyor.
              </Alert>
            )}
          </TabPanel>

          {/* Kullanım Tab */}
          <TabPanel value={tabValue} index={3}>
            <Typography variant="h5" sx={{ fontWeight: 700, mb: 3, color: 'primary.main' }}>
              Kullanım Özeti - {selectedPeriod}
            </Typography>
            {usageSummary ? (
              <Box sx={{ 
                display: 'grid', 
                gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' },
                gap: 3 
              }}>
                <Box>
                  <Card sx={{ 
                    borderRadius: 3, 
                    textAlign: 'center',
                    background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                    color: 'white',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 6,
                    },
                    transition: 'all 0.3s ease'
                  }}>
                    <CardContent>
                      <Wifi sx={{ fontSize: 48, color: 'white', mb: 2 }} />
                      <Typography variant="h4" sx={{ fontWeight: 700, color: 'white' }}>
                        {usageSummary.dataUsage || 0}GB
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                        Data Kullanımı
                      </Typography>
                    </CardContent>
                  </Card>
                </Box>
                
                <Box>
                  <Card sx={{ 
                    borderRadius: 3, 
                    textAlign: 'center',
                    background: 'linear-gradient(135deg, #00C851 0%, #00994A 100%)',
                    color: 'white',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 6,
                    },
                    transition: 'all 0.3s ease'
                  }}>
                    <CardContent>
                      <Phone sx={{ fontSize: 48, color: 'white', mb: 2 }} />
                      <Typography variant="h4" sx={{ fontWeight: 700, color: 'white' }}>
                        {usageSummary.voiceUsage || 0}dk
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                        Ses Kullanımı
                      </Typography>
                    </CardContent>
                  </Card>
                </Box>
                
                <Box>
                  <Card sx={{ 
                    borderRadius: 3, 
                    textAlign: 'center',
                    background: 'linear-gradient(135deg, #FF9800 0%, #F57C00 100%)',
                    color: 'white',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 6,
                    },
                    transition: 'all 0.3s ease'
                  }}>
                    <CardContent>
                      <Sms sx={{ fontSize: 48, color: 'white', mb: 2 }} />
                      <Typography variant="h4" sx={{ fontWeight: 700, color: 'white' }}>
                        {usageSummary.smsUsage || 0}
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                        SMS Kullanımı
                      </Typography>
                    </CardContent>
                  </Card>
                </Box>
                
                <Box>
                  <Card sx={{ 
                    borderRadius: 3, 
                    textAlign: 'center',
                    background: 'linear-gradient(135deg, #2196F3 0%, #1976D2 100%)',
                    color: 'white',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 6,
                    },
                    transition: 'all 0.3s ease'
                  }}>
                    <CardContent>
                      <Public sx={{ fontSize: 48, color: 'white', mb: 2 }} />
                      <Typography variant="h4" sx={{ fontWeight: 700, color: 'white' }}>
                        {usageSummary.roamingUsage || 0}MB
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                        Roaming
                      </Typography>
                    </CardContent>
                  </Card>
                </Box>
              </Box>
            ) : (
              <Alert severity="info">Kullanım verisi bulunamadı.</Alert>
            )}
          </TabPanel>

          {/* Simülasyon Tab */}
          <TabPanel value={tabValue} index={4}>
            <Typography variant="h5" sx={{ fontWeight: 700, mb: 3, color: 'primary.main' }}>
              What-If Simülasyonu
            </Typography>
            <Alert severity="info" sx={{ mb: 3 }}>
              Farklı plan ve paket kombinasyonlarını simüle ederek tasarruf fırsatlarını keşfedin.
            </Alert>
            
            <Box sx={{ 
              display: 'grid', 
              gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
              gap: 3 
            }}>
              <Box>
                <Card sx={{ borderRadius: 3 }}>
                  <CardContent>
                    <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                      Mevcut Durum
                    </Typography>
                    <Box sx={{ p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
                      <Typography variant="body2">
                        <strong>Plan:</strong> Bireysel 5GB
                      </Typography>
                      <Typography variant="body2">
                        <strong>Aylık Tutar:</strong> ₺45
                      </Typography>
                      <Typography variant="body2">
                        <strong>Ek Paketler:</strong> Yok
                      </Typography>
                    </Box>
                  </CardContent>
                </Card>
              </Box>
              
              <Box>
                <Card sx={{ borderRadius: 3 }}>
                  <CardContent>
                    <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                      Simülasyon Sonucu
                    </Typography>
                    <Box sx={{ p: 2, bgcolor: 'success.50', borderRadius: 2 }}>
                      <Typography variant="body2" color="success.main">
                        <strong>Önerilen Plan:</strong> Bireysel 10GB
                      </Typography>
                      <Typography variant="body2" color="success.main">
                        <strong>Yeni Tutar:</strong> ₺40
                      </Typography>
                      <Typography variant="h6" sx={{ fontWeight: 700, color: 'success.main', mt: 1 }}>
                        Aylık Tasarruf: ₺5
                      </Typography>
                    </Box>
                  </CardContent>
                </Card>
              </Box>
            </Box>
            
            <Box sx={{ mt: 3, textAlign: 'center' }}>
              <Button
                variant="contained"
                size="large"
                startIcon={<CompareArrows />}
                sx={{
                  borderRadius: 3,
                  px: 4,
                  py: 1.5,
                  background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                  '&:hover': {
                    background: 'linear-gradient(135deg, #0077A3 0%, #005580 100%)',
                  },
                }}
              >
                Yeni Simülasyon Başlat
              </Button>
            </Box>
          </TabPanel>
        </Paper>

        {/* Refresh Button */}
        <Box sx={{ mt: 4, textAlign: 'center' }}>
          <Button
            variant="contained"
            startIcon={<Refresh />}
            onClick={loadDashboardData}
            sx={{ 
              borderRadius: 3, 
              px: 6,
              py: 2,
              background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
              boxShadow: '0 4px 20px rgba(0, 163, 224, 0.3)',
              '&:hover': {
                background: 'linear-gradient(135deg, #0077A3 0%, #005580 100%)',
                transform: 'translateY(-3px)',
                boxShadow: '0 8px 30px rgba(0, 163, 224, 0.4)',
              },
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
            }}
          >
            Verileri Yenile
          </Button>
        </Box>
      </Container>
    </Box>
  );
};

export default DashboardPage;
