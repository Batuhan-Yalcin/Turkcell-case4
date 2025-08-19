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
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  CardActions as MuiCardActions,
} from '@mui/material';
import {
  Lightbulb,
  Receipt,
  Analytics,
  TrendingUp,
  TrendingDown,
  Refresh,
  Search,
  CalendarToday,
  AccountBalance,
  CheckCircle,
  Error,
  Info,
  ExpandMore,
  SmartToy,
  Psychology,
  Insights,
  Assessment,
  AutoAwesome,
  School,
  Help,
  Visibility,
  Download,
  Share,
  Print,
  Star,
  Speed,
  Security,
  Support,
  Wifi,
  Phone,
  Sms,
  Public,
  ShoppingCart,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import apiService from '../../services/api';
import { Bill, ExplainRequest, ExplainResponse, BillSummary, CategoryBreakdown } from '../../types';

interface ExplainPageProps {}

const ExplainPage: React.FC<ExplainPageProps> = () => {
  const { user } = useAuth();
  const theme = useTheme();
  const [bills, setBills] = useState<Bill[]>([]);
  const [selectedBill, setSelectedBill] = useState<Bill | null>(null);
  const [explanation, setExplanation] = useState<ExplainResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isExplaining, setIsExplaining] = useState(false);
  const [error, setError] = useState('');
  const [filterPeriod, setFilterPeriod] = useState('');
  const [availablePeriods, setAvailablePeriods] = useState<string[]>([]);
  const [showExplanationDialog, setShowExplanationDialog] = useState(false);
  const [showBillSelectionDialog, setShowBillSelectionDialog] = useState(false);

  useEffect(() => {
    loadBillsData();
  }, []);

  useEffect(() => {
    // Dönem seçimi değiştiğinde faturaları yeniden yükle
    if (filterPeriod) {
      loadBillsData();
    }
  }, [filterPeriod]);

  const loadBillsData = async () => {
    try {
      setIsLoading(true);
      setError('');
      
      // Kullanıcı kimliğini al: seçilen kullanıcı -> oturum kullanıcısı -> 1001 fallback
      const userId = user?.userId || 1001;
      
      // Load available periods
      const periodsResponse = await apiService.getAvailablePeriods(userId);
      setAvailablePeriods(periodsResponse.data);
      
      // Varsayılan dönem ayarla
      if (!filterPeriod && periodsResponse.data && periodsResponse.data.length > 0) {
        setFilterPeriod(periodsResponse.data[0]);
      }
      
      // Load recent bills
      const billsResponse = await apiService.getRecentBillsByUserId(userId);
      setBills(billsResponse.data);
      
    } catch (err: any) {
      setError(err.response?.data?.message || 'Fatura verileri yüklenirken bir hata oluştu');
      setBills([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleExplainBill = async (bill: Bill) => {
    try {
      setIsExplaining(true);
      setSelectedBill(bill);
      
      const request: ExplainRequest = {
        billId: bill.billId
      };
      
      const response = await apiService.explainBill(request);
      setExplanation(response.data);
      setShowExplanationDialog(true);
      
    } catch (err: any) {
      setError('Fatura açıklaması oluşturulurken bir hata oluştu');
    } finally {
      setIsExplaining(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'paid':
      case 'success':
        return 'success';
      case 'pending':
      case 'warning':
        return 'warning';
      case 'overdue':
      case 'error':
        return 'error';
      default:
        return 'default';
    }
  };

  if (isLoading && bills.length === 0) {
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
              background: 'linear-gradient(135deg, #9C27B0 0%, #673AB7 100%)',
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
                  <SmartToy />
                </Avatar>
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
                    AI Fatura Asistanı
                  </Typography>
                  <Typography variant="h6" sx={{ opacity: 0.9, fontWeight: 500 }}>
                    Faturalarınızı yapay zeka ile analiz edin ve anlayın
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
                  icon={<Receipt />}
                  label={`Toplam: ${bills.length} Fatura`}
                  sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', color: 'white' }}
                />
                <Chip
                  icon={<SmartToy />}
                  label="AI Destekli Analiz"
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

        {/* AI Features Overview */}
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' },
          gap: 3, 
          mb: 4 
        }}>
          <Box>
            <Grow in={true} timeout={1000}>
              <Card sx={{ 
                borderRadius: 3, 
                boxShadow: 3,
                background: 'linear-gradient(135deg, #9C27B0 0%, #673AB7 100%)',
                color: 'white',
                '&:hover': {
                  transform: 'translateY(-6px)',
                  boxShadow: '0 12px 40px rgba(156, 39, 176, 0.4)',
                },
                transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
              }}>
                <CardContent sx={{ textAlign: 'center', p: 3 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                    <Psychology sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h6" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    Akıllı Analiz
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Fatura kalemlerini otomatik analiz eder
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
                background: 'linear-gradient(135deg, #00BCD4 0%, #0097A7 100%)',
                color: 'white',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 6,
                },
                transition: 'all 0.3s ease'
              }}>
                <CardContent sx={{ textAlign: 'center', p: 3 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                    <Insights sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h6" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    Doğal Dil
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Türkçe açıklamalar ile anlaşılır
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
                background: 'linear-gradient(135deg, #4CAF50 0%, #388E3C 100%)',
                color: 'white',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 6,
                },
                transition: 'all 0.3s ease'
              }}>
                <CardContent sx={{ textAlign: 'center', p: 3 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', mx: 'auto', mb: 2, width: 56, height: 56 }}>
                    <Lightbulb sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h6" sx={{ fontWeight: 700, color: 'white', mb: 1 }}>
                    Tasarruf Önerileri
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
                    Kişiselleştirilmiş tasarruf ipuçları
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
                {availablePeriods.map((period) => (
                  <MenuItem key={period} value={period}>
                    {period}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            
            <Button
              variant="outlined"
              startIcon={<Refresh />}
              onClick={loadBillsData}
              sx={{ borderRadius: 2 }}
            >
              Yenile
            </Button>
            
            <Button
              variant="contained"
              startIcon={<SmartToy />}
              onClick={() => setShowBillSelectionDialog(true)}
              sx={{
                borderRadius: 2,
                background: 'linear-gradient(135deg, #9C27B0 0%, #673AB7 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #673AB7 0%, #5E35B1 100%)',
                },
              }}
            >
              AI Analizi Başlat
            </Button>
          </Box>
        </Paper>

        {/* Bills Table */}
        <Paper sx={{ borderRadius: 4, boxShadow: 3, overflow: 'hidden' }}>
          <Box sx={{ p: 3, borderBottom: 1, borderColor: 'divider' }}>
            <Typography variant="h5" sx={{ fontWeight: 700, display: 'flex', alignItems: 'center' }}>
              <Receipt sx={{ mr: 1, color: 'primary.main' }} />
              Fatura Listesi
            </Typography>
          </Box>
          
          {bills.length > 0 ? (
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.50' }}>
                    <TableCell sx={{ fontWeight: 700 }}>Fatura ID</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Dönem</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Toplam Tutar</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Durum</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>İşlemler</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {bills.map((bill) => (
                    <TableRow key={bill.billId} hover>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          #{bill.billId}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <CalendarToday sx={{ fontSize: 16, mr: 1, color: 'text.secondary' }} />
                          <Typography variant="body2">
                            {bill.periodStart && bill.periodEnd ? 
                              `${bill.periodStart} - ${bill.periodEnd}` : 'N/A'
                            }
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
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
                        <Button
                          size="small"
                          startIcon={<SmartToy />}
                          variant="contained"
                          onClick={() => handleExplainBill(bill)}
                          disabled={isExplaining}
                          sx={{ 
                            borderRadius: 2,
                            background: 'linear-gradient(135deg, #9C27B0 0%, #673AB7 100%)',
                            '&:hover': {
                              background: 'linear-gradient(135deg, #673AB7 0%, #5E35B1 100%)',
                            },
                          }}
                        >
                          {isExplaining ? <CircularProgress size={16} /> : 'AI Analiz'}
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Box sx={{ p: 4, textAlign: 'center' }}>
              <Receipt sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                Henüz fatura bulunmuyor
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Seçilen dönemde fatura bulunamadı
              </Typography>
            </Box>
          )}
        </Paper>

        {/* AI Explanation Dialog */}
        <Dialog
          open={showExplanationDialog}
          onClose={() => setShowExplanationDialog(false)}
          maxWidth="lg"
          fullWidth
        >
          <DialogTitle sx={{ 
            display: 'flex', 
            alignItems: 'center',
            background: 'linear-gradient(135deg, #9C27B0 0%, #673AB7 100%)',
            color: 'white'
          }}>
            <SmartToy sx={{ mr: 1 }} />
            AI Fatura Analizi - #{selectedBill?.billId}
          </DialogTitle>
          <DialogContent sx={{ pt: 3 }}>
            {explanation && (
              <Box>
                {/* AI Summary */}
                <Card sx={{ mb: 3, borderRadius: 3, border: '2px solid', borderColor: 'primary.200' }}>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      <AutoAwesome sx={{ mr: 1, color: 'primary.main' }} />
                      <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
                        AI Özeti
                      </Typography>
                    </Box>
                    <Typography variant="body1" sx={{ 
                      p: 2, 
                      bgcolor: 'primary.50', 
                      borderRadius: 2,
                      fontStyle: 'italic',
                      lineHeight: 1.6
                    }}>
                      {explanation.naturalLanguageSummary || 'AI analizi yapılamadı.'}
                    </Typography>
                  </CardContent>
                </Card>

                {/* Bill Summary */}
                {explanation.summary && (
                  <Card sx={{ mb: 3, borderRadius: 3 }}>
                    <CardContent>
                      <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, display: 'flex', alignItems: 'center' }}>
                        <Assessment sx={{ mr: 1, color: 'info.main' }} />
                        Fatura Özeti
                      </Typography>
                      <Box sx={{ 
                        display: 'grid', 
                        gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' },
                        gap: 2 
                      }}>
                        <Box sx={{ p: 2, bgcolor: 'primary.50', borderRadius: 2, textAlign: 'center' }}>
                          <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main' }}>
                            ₺{explanation.summary.totalAmount || 0}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            Toplam Tutar
                          </Typography>
                        </Box>
                        <Box sx={{ p: 2, bgcolor: 'warning.50', borderRadius: 2, textAlign: 'center' }}>
                          <Typography variant="h4" sx={{ fontWeight: 700, color: 'warning.main' }}>
                            ₺{explanation.summary.taxes || 0}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            Vergi
                          </Typography>
                        </Box>
                        <Box sx={{ p: 2, bgcolor: 'info.50', borderRadius: 2, textAlign: 'center' }}>
                          <Typography variant="h4" sx={{ fontWeight: 700, color: 'info.main' }}>
                            ₺{explanation.summary.usageBasedCharges || 0}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            Kullanım Ücreti
                          </Typography>
                        </Box>
                        <Box sx={{ p: 2, bgcolor: 'success.50', borderRadius: 2, textAlign: 'center' }}>
                          <Typography variant="h4" sx={{ fontWeight: 700, color: 'success.main' }}>
                            ₺{explanation.summary.oneTimeCharges || 0}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            Tek Seferlik
                          </Typography>
                        </Box>
                      </Box>
                      
                      {explanation.summary.savingsHint && (
                        <Box sx={{ mt: 3, p: 2, bgcolor: 'success.50', borderRadius: 2, border: '1px solid', borderColor: 'success.200' }}>
                          <Typography variant="body1" sx={{ display: 'flex', alignItems: 'center', color: 'success.main', fontWeight: 600 }}>
                            <Lightbulb sx={{ mr: 1 }} />
                            {explanation.summary.savingsHint}
                          </Typography>
                        </Box>
                      )}
                    </CardContent>
                  </Card>
                )}

                {/* Category Breakdown */}
                {explanation.breakdown && explanation.breakdown.length > 0 && (
                  <Card sx={{ borderRadius: 3 }}>
                    <CardContent>
                      <Typography variant="h6" sx={{ fontWeight: 700, mb: 2, display: 'flex', alignItems: 'center' }}>
                        <Analytics sx={{ mr: 1, color: 'secondary.main' }} />
                        Kategori Bazlı Analiz
                      </Typography>
                      <Box sx={{ display: 'grid', gap: 2 }}>
                        {explanation.breakdown.map((category, index) => (
                          <Accordion key={index} sx={{ borderRadius: 2, '&:before': { display: 'none' } }}>
                            <AccordionSummary expandIcon={<ExpandMore />}>
                              <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', flex: 1 }}>
                                  {category.category === 'DATA' && <Wifi sx={{ mr: 1, color: 'primary.main' }} />}
                                  {category.category === 'VOICE' && <Phone sx={{ mr: 1, color: 'success.main' }} />}
                                  {category.category === 'SMS' && <Sms sx={{ mr: 1, color: 'info.main' }} />}
                                  {category.category === 'ROAMING' && <Public sx={{ mr: 1, color: 'warning.main' }} />}
                                  {category.category === 'VAS' && <ShoppingCart sx={{ mr: 1, color: 'secondary.main' }} />}
                                  {category.category === 'TAX' && <AccountBalance sx={{ mr: 1, color: 'error.main' }} />}
                                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                                    {category.category}
                                  </Typography>
                                </Box>
                                <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
                                  ₺{category.total || 0}
                                </Typography>
                              </Box>
                            </AccordionSummary>
                            <AccordionDetails>
                              <Box sx={{ p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
                                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                  {category.explanation || 'Bu kategori için açıklama bulunmuyor.'}
                                </Typography>
                                
                                {category.lines && category.lines.length > 0 && (
                                  <TableContainer>
                                    <Table size="small">
                                      <TableHead>
                                        <TableRow>
                                          <TableCell>Açıklama</TableCell>
                                          <TableCell>Miktar</TableCell>
                                          <TableCell>Birim Fiyat</TableCell>
                                          <TableCell>Toplam</TableCell>
                                        </TableRow>
                                      </TableHead>
                                      <TableBody>
                                        {category.lines.map((line, lineIndex) => (
                                          <TableRow key={lineIndex}>
                                            <TableCell>{line.description}</TableCell>
                                            <TableCell>{line.quantity}</TableCell>
                                            <TableCell>₺{line.unitPrice}</TableCell>
                                            <TableCell>
                                              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                                                ₺{line.amount}
                                              </Typography>
                                            </TableCell>
                                          </TableRow>
                                        ))}
                                      </TableBody>
                                    </Table>
                                  </TableContainer>
                                )}
                              </Box>
                            </AccordionDetails>
                          </Accordion>
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
              onClick={() => setShowExplanationDialog(false)}
              variant="outlined"
              sx={{ borderRadius: 2 }}
            >
              Kapat
            </Button>
            <Button
              variant="contained"
              startIcon={<Download />}
              sx={{
                borderRadius: 2,
                background: 'linear-gradient(135deg, #4CAF50 0%, #388E3C 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #388E3C 0%, #2E7D32 100%)',
                },
              }}
            >
              PDF İndir
            </Button>
            <Button
              variant="contained"
              startIcon={<Share />}
              sx={{
                borderRadius: 2,
                background: 'linear-gradient(135deg, #9C27B0 0%, #673AB7 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #673AB7 0%, #5E35B1 100%)',
                },
              }}
            >
              Paylaş
            </Button>
          </DialogActions>
        </Dialog>

        {/* Bill Selection Dialog */}
        <Dialog
          open={showBillSelectionDialog}
          onClose={() => setShowBillSelectionDialog(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle sx={{ 
            display: 'flex', 
            alignItems: 'center',
            background: 'linear-gradient(135deg, #9C27B0 0%, #673AB7 100%)',
            color: 'white'
          }}>
            <SmartToy sx={{ mr: 1 }} />
            Fatura Seçimi
          </DialogTitle>
          <DialogContent sx={{ pt: 3 }}>
            <Typography variant="body1" sx={{ mb: 3 }}>
              AI analizi yapmak istediğiniz faturayı seçin:
            </Typography>
            {bills.length > 0 ? (
              <Box sx={{ display: 'grid', gap: 2 }}>
                {bills.map((bill) => (
                  <Card key={bill.billId} sx={{ borderRadius: 2, cursor: 'pointer' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <Box>
                          <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                            Fatura #{bill.billId}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {bill.periodStart} - {bill.periodEnd}
                          </Typography>
                        </Box>
                        <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
                          ₺{bill.totalAmount}
                        </Typography>
                      </Box>
                    </CardContent>
                    <MuiCardActions>
                      <Button
                        fullWidth
                        startIcon={<SmartToy />}
                        onClick={() => {
                          setShowBillSelectionDialog(false);
                          handleExplainBill(bill);
                        }}
                        sx={{
                          borderRadius: 2,
                          background: 'linear-gradient(135deg, #9C27B0 0%, #673AB7 100%)',
                          '&:hover': {
                            background: 'linear-gradient(135deg, #673AB7 0%, #5E35B1 100%)',
                          },
                        }}
                      >
                        AI Analiz Başlat
                      </Button>
                    </MuiCardActions>
                  </Card>
                ))}
              </Box>
            ) : (
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <Receipt sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
                <Typography variant="h6" color="text.secondary">
                  Fatura bulunamadı
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Seçilen dönemde fatura bulunmuyor
                </Typography>
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowBillSelectionDialog(false)}>İptal</Button>
          </DialogActions>
        </Dialog>
      </Container>
    </Box>
  );
};

export default ExplainPage;
