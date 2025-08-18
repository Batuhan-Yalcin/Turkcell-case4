import React, { useState, useEffect } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  Chip,
  LinearProgress,
  IconButton,
  Avatar,
  useTheme,
  Fade,
  Grow,
  Alert,
} from '@mui/material';
import {
  Receipt,
  TrendingUp,
  Warning,
  AccountBalance,
  MoreVert,
  CheckCircle,
  Notifications,
  TrendingDown,
  Speed,
  Visibility,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { Bill, Anomaly } from '../../types';

// Styled components
const StatsCard = ({ 
  title, 
  value, 
  icon, 
  gradient, 
  trend, 
  trendValue, 
  trendDirection = 'up' 
}: {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  gradient: string;
  trend?: string;
  trendValue?: string;
  trendDirection?: 'up' | 'down';
}) => (
  <Card
    sx={{
      background: gradient,
      color: 'white',
      position: 'relative',
      overflow: 'hidden',
      borderRadius: 4,
      transition: 'all 0.3s ease',
      '&:hover': {
        transform: 'translateY(-8px)',
        boxShadow: '0px 20px 40px rgba(0,0,0,0.3)',
      },
    }}
  >
    <CardContent sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        <Avatar
          sx={{
            bgcolor: 'rgba(255,255,255,0.2)',
            mr: 2,
            width: 48,
            height: 48,
          }}
        >
          {icon}
        </Avatar>
        <IconButton
          size="small"
          sx={{ color: 'white', ml: 'auto', opacity: 0.7 }}
        >
          <MoreVert />
        </IconButton>
      </Box>
      <Typography variant="h3" sx={{ fontWeight: 700, mb: 1, fontSize: '2.5rem' }}>
        {value}
      </Typography>
      <Typography variant="body1" sx={{ opacity: 0.9, mb: 1, fontWeight: 500 }}>
        {title}
      </Typography>
      {trend && (
        <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
          {trendDirection === 'up' ? (
            <TrendingUp sx={{ fontSize: 16, mr: 0.5, color: 'rgba(255,255,255,0.8)' }} />
          ) : (
            <TrendingDown sx={{ fontSize: 16, mr: 0.5, color: 'rgba(255,255,255,0.8)' }} />
          )}
          <Typography variant="caption" sx={{ opacity: 0.8 }}>
            {trend} {trendValue}
          </Typography>
        </Box>
      )}
    </CardContent>
  </Card>
);

const DashboardPage: React.FC = () => {
  const theme = useTheme();
  const { user } = useAuth();
  const [recentBills, setRecentBills] = useState<Bill[]>([]);
  const [anomalies, setAnomalies] = useState<Anomaly[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setIsLoading(true);
        // Demo veriler için mock data kullanıyoruz
        const mockBills: Bill[] = [
          {
            id: 1,
            userId: 1,
            period: '2024-01',
            totalAmount: 89.99,
            taxAmount: 16.19,
            netAmount: 73.80,
            dueDate: '2024-02-15',
            status: 'PAID',
            items: [],
            createdAt: '2024-01-31',
            updatedAt: '2024-01-31',
          },
          {
            id: 2,
            userId: 1,
            period: '2023-12',
            totalAmount: 79.99,
            taxAmount: 14.39,
            netAmount: 65.60,
            dueDate: '2024-01-15',
            status: 'PAID',
            items: [],
            createdAt: '2023-12-31',
            updatedAt: '2023-12-31',
          },
        ];

        const mockAnomalies: Anomaly[] = [
          {
            id: 1,
            userId: 1,
            billId: 1,
            type: 'UNUSUAL_USAGE',
            severity: 'MEDIUM',
            description: 'Veri kullanımında %25 artış tespit edildi',
            detectedAt: '2024-01-28',
            status: 'DETECTED',
          },
        ];

        setRecentBills(mockBills);
        setAnomalies(mockAnomalies);
      } catch (error) {
        console.error('Dashboard data fetch failed:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PAID':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'OVERDUE':
        return 'error';
      default:
        return 'default';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'HIGH':
        return 'error';
      case 'MEDIUM':
        return 'warning';
      case 'LOW':
        return 'info';
      default:
        return 'default';
    }
  };

  const totalBills = recentBills.length;
  const totalAmount = recentBills.reduce((sum, bill) => sum + bill.totalAmount, 0);
  const averageAmount = totalBills > 0 ? totalAmount / totalBills : 0;
  const activeAnomalies = anomalies.filter(a => a.status === 'DETECTED').length;

  if (isLoading) {
    return (
      <Box sx={{ width: '100%' }}>
        <LinearProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 0 }}>
      {/* Welcome Section */}
      <Grow in={true} timeout={800}>
        <Box sx={{ mb: 4 }}>
          <Typography variant="h3" sx={{ 
            fontWeight: 800, 
            mb: 2,
            background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
            backgroundClip: 'text',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            textAlign: 'center',
          }}>
            Hoş Geldiniz, {user?.msisdn || 'Kullanıcı'}! 👋
          </Typography>
          <Typography variant="h6" sx={{ 
            color: 'text.secondary', 
            textAlign: 'center',
            fontWeight: 400,
            opacity: 0.8,
          }}>
            Fatura durumunuzu ve kullanım analizlerinizi takip edin
          </Typography>
        </Box>
      </Grow>

      {/* Stats Cards */}
      <Box sx={{ 
        display: 'grid', 
        gridTemplateColumns: { 
          xs: '1fr', 
          sm: 'repeat(2, 1fr)', 
          md: 'repeat(4, 1fr)' 
        }, 
        gap: 3, 
        mb: 4 
      }}>
        <Grow in={true} timeout={1000}>
          <div>
            <StatsCard
              title="Toplam Fatura"
              value={totalBills}
              icon={<Receipt />}
              gradient="linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)"
              trend="Bu ay"
              trendValue="+2"
            />
          </div>
        </Grow>

        <Grow in={true} timeout={1200}>
          <div>
            <StatsCard
              title="Ortalama Fatura"
              value={`₺${averageAmount.toFixed(2)}`}
              icon={<AccountBalance />}
              gradient="linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)"
              trend="Geçen aya göre"
              trendValue="+12%"
            />
          </div>
        </Grow>

        <Grow in={true} timeout={1400}>
          <div>
            <StatsCard
              title="Toplam Tutar"
              value={`₺${totalAmount.toFixed(2)}`}
              icon={<TrendingUp />}
              gradient="linear-gradient(135deg, #00C851 0%, #00994A 100%)"
              trend="Bu dönem"
              trendValue="+8.5%"
            />
          </div>
        </Grow>

        <Grow in={true} timeout={1600}>
          <div>
            <StatsCard
              title="Aktif Anomali"
              value={activeAnomalies}
              icon={<Warning />}
              gradient="linear-gradient(135deg, #FF9800 0%, #F57C00 100%)"
              trend="Son 7 gün"
              trendValue="-1"
              trendDirection="down"
            />
          </div>
        </Grow>
      </Box>

      {/* Main Content Grid */}
      <Box sx={{ 
        display: 'grid', 
        gridTemplateColumns: { 
          xs: '1fr', 
          md: '2fr 1fr' 
        }, 
        gap: 3 
      }}>
        {/* Recent Bills */}
        <Grow in={true} timeout={1800}>
          <Card sx={{ 
            borderRadius: 4, 
            boxShadow: '0px 8px 32px rgba(0,0,0,0.08)',
            border: '1px solid rgba(0,0,0,0.05)',
          }}>
            <CardContent sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <Typography variant="h5" sx={{ fontWeight: 700, display: 'flex', alignItems: 'center' }}>
                  <Receipt sx={{ mr: 1, color: 'primary.main' }} />
                  Son Faturalar
                </Typography>
                <Box sx={{ ml: 'auto' }}>
                  <Chip
                    label={`${recentBills.length} fatura`}
                    size="small"
                    color="primary"
                    sx={{ fontWeight: 600 }}
                  />
                </Box>
              </Box>

              {recentBills.map((bill, index) => (
                <Fade in={true} timeout={2000 + index * 200} key={bill.id}>
                  <Box
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      p: 2.5,
                      mb: 2,
                      borderRadius: 3,
                      border: '1px solid',
                      borderColor: 'divider',
                      backgroundColor: 'background.paper',
                      transition: 'all 0.3s ease',
                      '&:hover': {
                        bgcolor: 'action.hover',
                        transform: 'translateX(8px)',
                        boxShadow: '0px 4px 16px rgba(0,0,0,0.1)',
                      },
                    }}
                  >
                    <Avatar
                      sx={{
                        bgcolor: theme.palette.primary.main,
                        mr: 2,
                        width: 48,
                        height: 48,
                        boxShadow: '0px 4px 12px rgba(230, 0, 0, 0.3)',
                      }}
                    >
                      <Receipt />
                    </Avatar>
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>
                        {bill.period} Dönemi
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                        Son Ödeme: {new Date(bill.dueDate).toLocaleDateString('tr-TR')}
                      </Typography>
                    </Box>
                    <Box sx={{ textAlign: 'right', mr: 2 }}>
                      <Typography variant="h5" sx={{ 
                        fontWeight: 800, 
                        color: 'primary.main',
                        mb: 1,
                      }}>
                        ₺{bill.totalAmount.toFixed(2)}
                      </Typography>
                      <Chip
                        label={bill.status === 'PAID' ? 'Ödendi' : 'Bekliyor'}
                        color={getStatusColor(bill.status) as any}
                        size="small"
                        sx={{ fontWeight: 600 }}
                      />
                    </Box>
                  </Box>
                </Fade>
              ))}
            </CardContent>
          </Card>
        </Grow>

        {/* Anomaly Status */}
        <Grow in={true} timeout={2000}>
          <Card sx={{ 
            borderRadius: 4, 
            boxShadow: '0px 8px 32px rgba(0,0,0,0.08)',
            border: '1px solid rgba(0,0,0,0.05)',
          }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h5" sx={{ 
                fontWeight: 700, 
                mb: 3, 
                display: 'flex', 
                alignItems: 'center',
                color: 'primary.main',
              }}>
                <Warning sx={{ mr: 1 }} />
                Anomali Durumu
              </Typography>

              {anomalies.length > 0 ? (
                anomalies.map((anomaly, index) => (
                  <Fade in={true} timeout={2200 + index * 200} key={anomaly.id}>
                    <Box
                      sx={{
                        p: 2.5,
                        mb: 2,
                        borderRadius: 3,
                        border: '1px solid',
                        borderColor: 'divider',
                        bgcolor: 'background.paper',
                        transition: 'all 0.3s ease',
                        '&:hover': {
                          transform: 'scale(1.02)',
                          boxShadow: '0px 4px 16px rgba(0,0,0,0.1)',
                        },
                      }}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1.5 }}>
                        <Chip
                          label={anomaly.severity === 'HIGH' ? 'Yüksek' : anomaly.severity === 'MEDIUM' ? 'Orta' : 'Düşük'}
                          color={getSeverityColor(anomaly.severity) as any}
                          size="small"
                          sx={{ mr: 1, fontWeight: 600 }}
                        />
                        <Chip
                          label={anomaly.type === 'UNUSUAL_USAGE' ? 'Kullanım' : 'Fiyat'}
                          variant="outlined"
                          size="small"
                          sx={{ fontWeight: 500 }}
                        />
                      </Box>
                      <Typography variant="body1" sx={{ mb: 1.5, fontWeight: 500 }}>
                        {anomaly.description}
                      </Typography>
                      <Typography variant="caption" sx={{ 
                        color: 'text.secondary',
                        display: 'flex',
                        alignItems: 'center',
                      }}>
                        <Visibility sx={{ fontSize: 14, mr: 0.5 }} />
                        {new Date(anomaly.detectedAt).toLocaleDateString('tr-TR')}
                      </Typography>
                    </Box>
                  </Fade>
                ))
              ) : (
                <Fade in={true} timeout={2400}>
                  <Box sx={{ textAlign: 'center', py: 4 }}>
                    <CheckCircle sx={{ 
                      fontSize: 64, 
                      color: 'success.main', 
                      mb: 2,
                      opacity: 0.8,
                    }} />
                    <Typography variant="h6" sx={{ 
                      color: 'success.main', 
                      mb: 1,
                      fontWeight: 600,
                    }}>
                      Anomali Tespit Edilmedi
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                      Sisteminiz normal çalışıyor
                    </Typography>
                  </Box>
                </Fade>
              )}
            </CardContent>
          </Card>
        </Grow>
      </Box>

      {/* Quick Actions */}
      <Grow in={true} timeout={2600}>
        <Box sx={{ mt: 4 }}>
          <Typography variant="h5" sx={{ 
            fontWeight: 700, 
            mb: 3,
            textAlign: 'center',
            color: 'text.primary',
          }}>
            Hızlı İşlemler
          </Typography>
          <Box sx={{ 
            display: 'grid', 
            gridTemplateColumns: { 
              xs: '1fr', 
              sm: 'repeat(2, 1fr)', 
              md: 'repeat(4, 1fr)' 
            }, 
            gap: 2 
          }}>
            {[
              { icon: <Receipt />, label: 'Fatura Görüntüle', color: '#E60000' },
              { icon: <Speed />, label: 'Kullanım Analizi', color: '#00A3E0' },
              { icon: <Warning />, label: 'Anomali Raporu', color: '#FF9800' },
              { icon: <TrendingUp />, label: 'Plan Karşılaştır', color: '#00C851' },
            ].map((action, index) => (
              <Fade in={true} timeout={2800 + index * 200} key={action.label}>
                <Card
                  sx={{
                    p: 2,
                    textAlign: 'center',
                    cursor: 'pointer',
                    transition: 'all 0.3s ease',
                    border: '2px solid transparent',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      borderColor: action.color,
                      boxShadow: `0px 8px 24px ${action.color}20`,
                    },
                  }}
                >
                  <Avatar
                    sx={{
                      bgcolor: action.color,
                      width: 56,
                      height: 56,
                      mx: 'auto',
                      mb: 1,
                    }}
                  >
                    {action.icon}
                  </Avatar>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {action.label}
                  </Typography>
                </Card>
              </Fade>
            ))}
          </Box>
        </Box>
      </Grow>
    </Box>
  );
};

export default DashboardPage;
