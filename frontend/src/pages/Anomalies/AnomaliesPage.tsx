import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  Card,
  CardContent,
  Button,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Alert,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  useTheme,
  Fade,
  Grow,
  Divider,
  Avatar,
  Badge,
  Stack,
  LinearProgress,
  Tooltip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  Warning,
  TrendingUp,
  TrendingDown,
  Refresh,
  FilterList,
  Search,
  CalendarToday,
  AccountBalance,
  CheckCircle,
  Error,
  Info,
  ExpandMore,
  Lightbulb,
  Security,
  Speed,
  Analytics,
  Timeline,
  Assessment,
  Notifications,
  Settings,
  Help,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import apiService from '../../services/api';
import { Anomaly, AnomalyRequest } from '../../types';

interface AnomaliesPageProps {}

const AnomaliesPage: React.FC<AnomaliesPageProps> = () => {
  const { user } = useAuth();
  const theme = useTheme();
  const [anomalies, setAnomalies] = useState<Anomaly[]>([]);
  const [selectedAnomaly, setSelectedAnomaly] = useState<Anomaly | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterPeriod, setFilterPeriod] = useState('2025-02');
  const [filterSeverity, setFilterSeverity] = useState('ALL');
  const [filterType, setFilterType] = useState('ALL');
  const [showAnomalyDialog, setShowAnomalyDialog] = useState(false);
  const [showDetectionDialog, setShowDetectionDialog] = useState(false);
  const [detectionRequest, setDetectionRequest] = useState<AnomalyRequest>({
    userId: 1001,
    period: '2025-02'
  });

  useEffect(() => {
    loadAnomaliesData();
  }, [filterPeriod, filterSeverity, filterType]);

  const loadAnomaliesData = async () => {
    try {
      setIsLoading(true);
      setError('');
      
      // Demo user ID: 1001
      const userId = 1001;
      
      // Load anomalies based on filters
      let anomaliesData: Anomaly[] = [];
      
      if (filterSeverity === 'ALL' && filterType === 'ALL') {
        const response = await apiService.getAnomalyHistory(userId);
        anomaliesData = response.data.anomalies || [];
      } else {
        // Apply filters
        const response = await apiService.getAnomalySummary(userId);
        anomaliesData = response.data.anomalies || [];
        
        if (filterSeverity !== 'ALL') {
          anomaliesData = anomaliesData.filter(a => a.severity === filterSeverity);
        }
        if (filterType !== 'ALL') {
          anomaliesData = anomaliesData.filter(a => a.type === filterType);
        }
      }
      
      setAnomalies(anomaliesData);
      
    } catch (err: any) {
      setError(err.response?.data?.message || 'Anomali verileri yüklenirken bir hata oluştu');
      // Fallback to mock data
      const mockAnomalies = [
        {
          anomalyId: 1,
          userId: 1001,
          billId: 700102,
          type: 'SPIKE',
          severity: 'HIGH',
          description: 'Data kullanımında anormal artış tespit edildi',
          detectedAt: new Date().toISOString(),
          status: 'ACTIVE',
          zScore: 2.5,
          percentageDifference: 15.5,
          recommendations: ['Data paketini kontrol edin', 'Kullanım limitlerini gözden geçirin']
        },
        {
          anomalyId: 2,
          userId: 1001,
          billId: 700102,
          type: 'NEW_ITEM',
          severity: 'MEDIUM',
          description: 'Yeni VAS servisi aktif edildi',
          detectedAt: new Date().toISOString(),
          status: 'ACTIVE',
          zScore: 1.8,
          percentageDifference: 8.2,
          recommendations: ['Servisi gerçekten kullanıyor musunuz?', 'Gereksizse iptal edin']
        },
        {
          anomalyId: 3,
          userId: 1001,
          billId: 700102,
          type: 'ROAMING_ACTIVATION',
          severity: 'LOW',
          description: 'Roaming servisi bu ay aktif edildi',
          detectedAt: new Date().toISOString(),
          status: 'RESOLVED',
          zScore: 1.2,
          percentageDifference: 5.1,
          recommendations: ['Roaming kullanımını kontrol edin', 'Gerekirse kapatın']
        }
      ];
      setAnomalies(mockAnomalies);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDetectAnomalies = async () => {
    try {
      setIsLoading(true);
      const response = await apiService.detectAnomalies(detectionRequest);
      setAnomalies(response.data.anomalies || []);
      setShowDetectionDialog(false);
    } catch (err: any) {
      setError('Anomali tespiti yapılırken bir hata oluştu');
    } finally {
      setIsLoading(false);
    }
  };

  const handleViewAnomaly = (anomaly: Anomaly) => {
    setSelectedAnomaly(anomaly);
    setShowAnomalyDialog(true);
  };

  const getSeverityColor = (severity: string) => {
    switch (severity?.toUpperCase()) {
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

  const getTypeIcon = (type: string) => {
    switch (type?.toUpperCase()) {
      case 'SPIKE':
        return <TrendingUp />;
      case 'NEW_ITEM':
        return <Info />;
      case 'ROAMING_ACTIVATION':
        return <Speed />;
      case 'PREMIUM_SMS_INCREASE':
        return <Notifications />;
      case 'VAS_INCREASE':
        return <Settings />;
      default:
        return <Warning />;
    }
  };

  const getTypeColor = (type: string) => {
    switch (type?.toUpperCase()) {
      case 'SPIKE':
        return 'error';
      case 'NEW_ITEM':
        return 'info';
      case 'ROAMING_ACTIVATION':
        return 'warning';
      case 'PREMIUM_SMS_INCREASE':
        return 'secondary';
      case 'VAS_INCREASE':
        return 'primary';
      default:
        return 'default';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'ACTIVE':
        return 'warning';
      case 'RESOLVED':
        return 'success';
      case 'INVESTIGATING':
        return 'info';
      default:
        return 'default';
    }
  };

  if (isLoading && anomalies.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'grey.50', py: 3 }}>
      <Container maxWidth="xl">
        {/* Header */}
        <Grow in={true} timeout={800}>
          <Paper
            elevation={0}
            sx={{
              p: 4,
              mb: 3,
              borderRadius: 4,
              background: 'linear-gradient(135deg, #FF6B35 0%, #F7931E 100%)',
              color: 'white',
              position: 'relative',
              overflow: 'hidden',
            }}
          >
            <Box
              sx={{
                position: 'absolute',
                top: -50,
                right: -50,
                width: 200,
                height: 200,
                borderRadius: '50%',
                background: 'rgba(255, 255, 255, 0.1)',
              }}
            />
            <Box sx={{ position: 'relative', zIndex: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Avatar
                  sx={{
                    bgcolor: 'rgba(255, 255, 255, 0.2)',
                    mr: 2,
                    width: 60,
                    height: 60,
                  }}
                >
                  <Warning />
                </Avatar>
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
                    Anomali Tespiti
                  </Typography>
                  <Typography variant="h6" sx={{ opacity: 0.9, fontWeight: 500 }}>
                    Faturalarınızdaki anormal durumları tespit edin ve çözün
                  </Typography>
                </Box>
              </Box>
              
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
                <Chip
                  icon={<CalendarToday />}
                  label={`Dönem: ${filterPeriod}`}
                  sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', color: 'white' }}
                />
                <Chip
                  icon={<Warning />}
                  label={`Toplam: ${anomalies.length} Anomali`}
                  sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', color: 'white' }}
                />
                <Chip
                  icon={<TrendingUp />}
                  label={`Aktif: ${anomalies.filter(a => a.status === 'ACTIVE').length}`}
                  sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', color: 'white' }}
                />
              </Box>
            </Box>
          </Paper>
        </Grow>

        {/* Error Alert */}
        {error && (
          <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
            {error}
          </Alert>
        )}

        {/* Quick Stats */}
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
                background: 'linear-gradient(135deg, #FF6B35 0%, #F7931E 100%)',
                color: 'white',
                '&:hover': {
                  transform: 'translateY(-6px)',
                  boxShadow: '0 12px 40px rgba(255, 107, 53, 0.4)',
                },
                transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
              }}>
                <CardContent sx={{ textAlign: 'center', p: 3 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                    <Warning sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    {anomalies.length}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Toplam Anomali
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
                    <Error sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    {anomalies.filter(a => a.severity === 'HIGH').length}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Yüksek Öncelik
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
                    <CheckCircle sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    {anomalies.filter(a => a.status === 'RESOLVED').length}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Çözüldü
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
                background: 'linear-gradient(135deg, #2196F3 0%, #1976D2 100%)',
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
                    {anomalies.filter(a => a.status === 'INVESTIGATING').length}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    İnceleniyor
                  </Typography>
                </CardContent>
              </Card>
            </Grow>
          </Box>
        </Box>

        {/* Filters and Actions */}
        <Paper sx={{ p: 3, mb: 3, borderRadius: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
            <FormControl sx={{ minWidth: 150 }}>
              <InputLabel>Dönem</InputLabel>
              <Select
                value={filterPeriod}
                label="Dönem"
                onChange={(e) => setFilterPeriod(e.target.value)}
              >
                <MenuItem value="2025-01">Ocak 2025</MenuItem>
                <MenuItem value="2025-02">Şubat 2025</MenuItem>
                <MenuItem value="2025-03">Mart 2025</MenuItem>
              </Select>
            </FormControl>
            
            <FormControl sx={{ minWidth: 150 }}>
              <InputLabel>Öncelik</InputLabel>
              <Select
                value={filterSeverity}
                label="Öncelik"
                onChange={(e) => setFilterSeverity(e.target.value)}
              >
                <MenuItem value="ALL">Tümü</MenuItem>
                <MenuItem value="HIGH">Yüksek</MenuItem>
                <MenuItem value="MEDIUM">Orta</MenuItem>
                <MenuItem value="LOW">Düşük</MenuItem>
              </Select>
            </FormControl>
            
            <FormControl sx={{ minWidth: 150 }}>
              <InputLabel>Tip</InputLabel>
              <Select
                value={filterType}
                label="Tip"
                onChange={(e) => setFilterType(e.target.value)}
              >
                <MenuItem value="ALL">Tümü</MenuItem>
                <MenuItem value="SPIKE">Spike</MenuItem>
                <MenuItem value="NEW_ITEM">Yeni Kalem</MenuItem>
                <MenuItem value="ROAMING_ACTIVATION">Roaming</MenuItem>
                <MenuItem value="PREMIUM_SMS_INCREASE">Premium SMS</MenuItem>
                <MenuItem value="VAS_INCREASE">VAS</MenuItem>
              </Select>
            </FormControl>
            
            <Button
              variant="outlined"
              startIcon={<Refresh />}
              onClick={loadAnomaliesData}
              sx={{ borderRadius: 2 }}
            >
              Yenile
            </Button>
            
            <Button
              variant="contained"
              startIcon={<Analytics />}
              onClick={() => setShowDetectionDialog(true)}
              sx={{
                borderRadius: 2,
                background: 'linear-gradient(135deg, #FF6B35 0%, #F7931E 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #F7931E 0%, #E67E00 100%)',
                },
              }}
            >
              Yeni Tespit
            </Button>
          </Box>
        </Paper>

        {/* Anomalies Table */}
        <Paper sx={{ borderRadius: 4, boxShadow: 3, overflow: 'hidden' }}>
          <Box sx={{ p: 3, borderBottom: 1, borderColor: 'divider' }}>
            <Typography variant="h5" sx={{ fontWeight: 700, display: 'flex', alignItems: 'center' }}>
              <Warning sx={{ mr: 1, color: 'warning.main' }} />
              Anomali Listesi
            </Typography>
          </Box>
          
          {anomalies.length > 0 ? (
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.50' }}>
                    <TableCell sx={{ fontWeight: 700 }}>Anomali ID</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Tip</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Öncelik</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Açıklama</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Durum</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Tespit Tarihi</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>İşlemler</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {anomalies.map((anomaly) => (
                    <TableRow key={anomaly.anomalyId} hover>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          #{anomaly.anomalyId}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          icon={getTypeIcon(anomaly.type)}
                          label={anomaly.type}
                          color={getTypeColor(anomaly.type) as any}
                          size="small"
                          sx={{ fontWeight: 600 }}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={anomaly.severity}
                          color={getSeverityColor(anomaly.severity) as any}
                          size="small"
                          sx={{ fontWeight: 600 }}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ maxWidth: 300 }}>
                          {anomaly.description}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={anomaly.status}
                          color={getStatusColor(anomaly.status) as any}
                          size="small"
                          sx={{ fontWeight: 600 }}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">
                          {new Date(anomaly.detectedAt).toLocaleDateString('tr-TR')}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          startIcon={<Info />}
                          variant="outlined"
                          onClick={() => handleViewAnomaly(anomaly)}
                          sx={{ borderRadius: 2 }}
                        >
                          Detay
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Box sx={{ p: 4, textAlign: 'center' }}>
              <CheckCircle sx={{ fontSize: 64, color: 'success.main', mb: 2 }} />
              <Typography variant="h6" color="success.main">
                Anomali Tespit Edilmedi
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Faturalarınız normal seyrediyor
              </Typography>
            </Box>
          )}
        </Paper>

        {/* Anomaly Details Dialog */}
        <Dialog
          open={showAnomalyDialog}
          onClose={() => setShowAnomalyDialog(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle sx={{ 
            display: 'flex', 
            alignItems: 'center',
            background: 'linear-gradient(135deg, #FF6B35 0%, #F7931E 100%)',
            color: 'white'
          }}>
            <Warning sx={{ mr: 1 }} />
            Anomali Detayları - #{selectedAnomaly?.anomalyId}
          </DialogTitle>
          <DialogContent sx={{ pt: 3 }}>
            {selectedAnomaly && (
              <Box>
                {/* Anomaly Summary */}
                <Card sx={{ mb: 3, borderRadius: 3 }}>
                  <CardContent>
                    <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                      Anomali Özeti
                    </Typography>
                    <Box sx={{ 
                      display: 'grid', 
                      gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
                      gap: 2 
                    }}>
                      <Box sx={{ p: 2, bgcolor: 'warning.50', borderRadius: 2, textAlign: 'center' }}>
                        <Typography variant="h4" sx={{ fontWeight: 700, color: 'warning.main' }}>
                          {selectedAnomaly.type}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Anomali Tipi
                        </Typography>
                      </Box>
                      <Box sx={{ p: 2, bgcolor: 'error.50', borderRadius: 2, textAlign: 'center' }}>
                        <Typography variant="h4" sx={{ fontWeight: 700, color: 'error.main' }}>
                          {selectedAnomaly.severity}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Öncelik
                        </Typography>
                      </Box>
                      <Box sx={{ p: 2, bgcolor: 'info.50', borderRadius: 2, textAlign: 'center' }}>
                        <Typography variant="h4" sx={{ fontWeight: 700, color: 'info.main' }}>
                          {selectedAnomaly.zScore?.toFixed(2) || 'N/A'}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Z-Score
                        </Typography>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>

                {/* Anomaly Details */}
                <Card sx={{ mb: 3, borderRadius: 3 }}>
                  <CardContent>
                    <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                      Detaylar
                    </Typography>
                    <Box sx={{ p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
                      <Typography variant="body1" sx={{ mb: 2 }}>
                        <strong>Açıklama:</strong> {selectedAnomaly.description}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        <strong>Tespit Tarihi:</strong> {new Date(selectedAnomaly.detectedAt).toLocaleString('tr-TR')}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        <strong>Durum:</strong> {selectedAnomaly.status}
                      </Typography>
                      {selectedAnomaly.percentageDifference && (
                        <Typography variant="body2" color="text.secondary">
                          <strong>Yüzde Değişim:</strong> %{selectedAnomaly.percentageDifference.toFixed(1)}
                        </Typography>
                      )}
                    </Box>
                  </CardContent>
                </Card>

                {/* Recommendations */}
                {selectedAnomaly.recommendations && selectedAnomaly.recommendations.length > 0 && (
                  <Card sx={{ borderRadius: 3 }}>
                    <CardContent>
                      <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, display: 'flex', alignItems: 'center' }}>
                        <Lightbulb sx={{ mr: 1, color: 'warning.main' }} />
                        Öneriler
                      </Typography>
                      <Box sx={{ display: 'grid', gap: 1 }}>
                        {selectedAnomaly.recommendations.map((rec, index) => (
                          <Box key={index} sx={{ 
                            p: 2, 
                            bgcolor: 'warning.50', 
                            borderRadius: 2,
                            border: '1px solid',
                            borderColor: 'warning.200'
                          }}>
                            <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center' }}>
                              <CheckCircle sx={{ mr: 1, color: 'warning.main', fontSize: 16 }} />
                              {rec}
                            </Typography>
                          </Box>
                        ))}
                      </Box>
                    </CardContent>
                  </Card>
                )}
              </Box>
            )}
          </DialogContent>
          <DialogActions sx={{ p: 3 }}>
            <Button
              onClick={() => setShowAnomalyDialog(false)}
              variant="outlined"
              sx={{ borderRadius: 2 }}
            >
              Kapat
            </Button>
            <Button
              variant="contained"
              startIcon={<CheckCircle />}
              sx={{
                borderRadius: 2,
                background: 'linear-gradient(135deg, #00C851 0%, #00994A 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #00994A 0%, #007A3D 100%)',
                },
              }}
            >
              Çözüldü Olarak İşaretle
            </Button>
          </DialogActions>
        </Dialog>

        {/* Detection Dialog */}
        <Dialog
          open={showDetectionDialog}
          onClose={() => setShowDetectionDialog(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle sx={{ 
            display: 'flex', 
            alignItems: 'center',
            background: 'linear-gradient(135deg, #FF6B35 0%, #F7931E 100%)',
            color: 'white'
          }}>
            <Analytics sx={{ mr: 1 }} />
            Yeni Anomali Tespiti
          </DialogTitle>
          <DialogContent sx={{ pt: 3 }}>
            <Box sx={{ display: 'grid', gap: 3 }}>
              <TextField
                fullWidth
                label="Kullanıcı ID"
                type="number"
                value={detectionRequest.userId}
                onChange={(e) => setDetectionRequest({
                  ...detectionRequest,
                  userId: parseInt(e.target.value)
                })}
              />
              <TextField
                fullWidth
                label="Dönem (YYYY-MM)"
                value={detectionRequest.period}
                onChange={(e) => setDetectionRequest({
                  ...detectionRequest,
                  period: e.target.value
                })}
                placeholder="2025-02"
              />
            </Box>
          </DialogContent>
          <DialogActions sx={{ p: 3 }}>
            <Button
              onClick={() => setShowDetectionDialog(false)}
              variant="outlined"
              sx={{ borderRadius: 2 }}
            >
              İptal
            </Button>
            <Button
              onClick={handleDetectAnomalies}
              variant="contained"
              startIcon={<Analytics />}
              sx={{
                borderRadius: 2,
                background: 'linear-gradient(135deg, #FF6B35 0%, #F7931E 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #F7931E 0%, #E67E00 100%)',
                },
              }}
            >
              Tespit Et
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </Box>
  );
};

export default AnomaliesPage;
