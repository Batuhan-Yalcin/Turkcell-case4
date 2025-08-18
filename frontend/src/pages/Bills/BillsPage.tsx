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
} from '@mui/material';
import {
  Receipt,
  Download,
  Visibility,
  FilterList,
  Search,
  CalendarToday,
  AccountBalance,
  TrendingUp,
  TrendingDown,
  Warning,
  CheckCircle,
  Refresh,
  FileDownload,
  Print,
  Share,
  MoreVert,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import apiService from '../../services/api';
import { Bill, BillSummary, BillItem } from '../../types';

interface BillsPageProps {}

const BillsPage: React.FC<BillsPageProps> = () => {
  const { user } = useAuth();
  const theme = useTheme();
  const [bills, setBills] = useState<Bill[]>([]);
  const [selectedBill, setSelectedBill] = useState<Bill | null>(null);
  const [billDetails, setBillDetails] = useState<BillSummary | null>(null);
  const [billItems, setBillItems] = useState<BillItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterPeriod, setFilterPeriod] = useState('2025-02');
  const [availablePeriods, setAvailablePeriods] = useState<string[]>([]);
  const [showBillDialog, setShowBillDialog] = useState(false);
  const [showDetailsDialog, setShowDetailsDialog] = useState(false);

  useEffect(() => {
    loadBillsData();
  }, [filterPeriod]);

  const loadBillsData = async () => {
    try {
      setIsLoading(true);
      setError('');
      
      // Demo user ID: 1001
      const userId = 1001;
      
      // Load available periods
      const periodsResponse = await apiService.getAvailablePeriods(userId);
      setAvailablePeriods(periodsResponse.data);
      
      // Load recent bills
      const billsResponse = await apiService.getRecentBillsByUserId(userId);
      setBills(billsResponse.data);
      
    } catch (err: any) {
      setError(err.response?.data?.message || 'Fatura verileri yüklenirken bir hata oluştu');
    } finally {
      setIsLoading(false);
    }
  };

  const handleViewBill = async (bill: Bill) => {
    try {
      setSelectedBill(bill);
      setShowBillDialog(true);
      
      // Load bill summary
      const summaryResponse = await apiService.getBillSummary(bill.billId);
      setBillDetails(summaryResponse.data);
      
      // Load bill items
      const itemsResponse = await apiService.getBillItemsByBillId(bill.billId);
      setBillItems(itemsResponse.data);
      
    } catch (err: any) {
      setError('Fatura detayları yüklenirken bir hata oluştu');
    }
  };

  const handleDownloadBill = (bill: Bill) => {
    // Mock download functionality
    console.log('Downloading bill:', bill.billId);
  };

  const handlePrintBill = (bill: Bill) => {
    // Mock print functionality
    console.log('Printing bill:', bill.billId);
  };

  const handleShareBill = (bill: Bill) => {
    // Mock share functionality
    console.log('Sharing bill:', bill.billId);
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

  const getStatusIcon = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'paid':
        return <CheckCircle />;
      case 'pending':
        return <Warning />;
      case 'overdue':
        return <TrendingDown />;
      default:
        return <Receipt />;
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
              background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
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
                  <Receipt />
                </Avatar>
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 700, mb: 0.5 }}>
                    Fatura Yönetimi
                  </Typography>
                  <Typography variant="h6" sx={{ opacity: 0.9, fontWeight: 500 }}>
                    Tüm faturalarınızı tek yerden takip edin
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
                  icon={<AccountBalance />}
                  label={`Toplam: ${bills.length} Fatura`}
                  sx={{ bgcolor: 'rgba(255, 255, 255, 0.2)', color: 'white' }}
                />
                <Chip
                  icon={<TrendingUp />}
                  label={`Toplam Tutar: ₺${bills.reduce((sum, bill) => sum + (bill.totalAmount || 0), 0).toFixed(2)}`}
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

        {/* Filters and Actions */}
        <Paper sx={{ p: 3, mb: 3, borderRadius: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
            <FormControl sx={{ minWidth: 200 }}>
              <InputLabel>Dönem Seçin</InputLabel>
              <Select
                value={filterPeriod}
                label="Dönem Seçin"
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
              startIcon={<FilterList />}
              sx={{
                borderRadius: 2,
                background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #0077A3 0%, #005580 100%)',
                },
              }}
            >
              Filtrele
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
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Button
                            size="small"
                            startIcon={<Visibility />}
                            variant="outlined"
                            onClick={() => handleViewBill(bill)}
                            sx={{ borderRadius: 2 }}
                          >
                            Görüntüle
                          </Button>
                          <Button
                            size="small"
                            startIcon={<Download />}
                            variant="outlined"
                            onClick={() => handleDownloadBill(bill)}
                            sx={{ borderRadius: 2 }}
                          >
                            İndir
                          </Button>
                          <IconButton
                            size="small"
                            onClick={() => setShowDetailsDialog(true)}
                          >
                            <MoreVert />
                          </IconButton>
                        </Box>
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

        {/* Bill Details Dialog */}
        <Dialog
          open={showBillDialog}
          onClose={() => setShowBillDialog(false)}
          maxWidth="md"
          fullWidth
        >
          <DialogTitle sx={{ 
            display: 'flex', 
            alignItems: 'center',
            background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
            color: 'white'
          }}>
            <Receipt sx={{ mr: 1 }} />
            Fatura Detayları - #{selectedBill?.billId}
          </DialogTitle>
          <DialogContent sx={{ pt: 3 }}>
            {selectedBill && billDetails && (
              <Box>
                {/* Bill Summary */}
                <Card sx={{ mb: 3, borderRadius: 3 }}>
                  <CardContent>
                    <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                      Fatura Özeti
                    </Typography>
                    <Box sx={{ 
                      display: 'grid', 
                      gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
                      gap: 2 
                    }}>
                      <Box sx={{ p: 2, bgcolor: 'primary.50', borderRadius: 2, textAlign: 'center' }}>
                        <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main' }}>
                          ₺{billDetails.totalAmount || 0}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Toplam Tutar
                        </Typography>
                      </Box>
                      <Box sx={{ p: 2, bgcolor: 'warning.50', borderRadius: 2, textAlign: 'center' }}>
                        <Typography variant="h4" sx={{ fontWeight: 700, color: 'warning.main' }}>
                          ₺{billDetails.taxAmount || 0}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Vergi
                        </Typography>
                      </Box>
                      <Box sx={{ p: 2, bgcolor: 'success.50', borderRadius: 2, textAlign: 'center' }}>
                        <Typography variant="h4" sx={{ fontWeight: 700, color: 'success.main' }}>
                          {billDetails.itemCount || 0}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Kalem Sayısı
                        </Typography>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>

                {/* Bill Items */}
                <Card sx={{ borderRadius: 3 }}>
                  <CardContent>
                    <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                      Fatura Kalemleri
                    </Typography>
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Kategori</TableCell>
                            <TableCell>Açıklama</TableCell>
                            <TableCell>Miktar</TableCell>
                            <TableCell>Birim Fiyat</TableCell>
                            <TableCell>Toplam</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {billItems.map((item) => (
                            <TableRow key={item.itemId}>
                              <TableCell>
                                <Chip
                                  label={item.category}
                                  size="small"
                                  color="primary"
                                  variant="outlined"
                                />
                              </TableCell>
                              <TableCell>{item.description}</TableCell>
                              <TableCell>{item.quantity}</TableCell>
                              <TableCell>₺{item.unitPrice}</TableCell>
                              <TableCell>
                                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                                  ₺{item.amount}
                                </Typography>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </CardContent>
                </Card>
              </Box>
            )}
          </DialogContent>
          <DialogActions sx={{ p: 3 }}>
            <Button
              onClick={() => setShowBillDialog(false)}
              variant="outlined"
              sx={{ borderRadius: 2 }}
            >
              Kapat
            </Button>
            <Button
              onClick={() => selectedBill && handlePrintBill(selectedBill)}
              variant="outlined"
              startIcon={<Print />}
              sx={{ borderRadius: 2 }}
            >
              Yazdır
            </Button>
            <Button
              onClick={() => selectedBill && handleDownloadBill(selectedBill)}
              variant="contained"
              startIcon={<FileDownload />}
              sx={{
                borderRadius: 2,
                background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #0077A3 0%, #005580 100%)',
                },
              }}
            >
              İndir
            </Button>
          </DialogActions>
        </Dialog>

        {/* Quick Actions Dialog */}
        <Dialog
          open={showDetailsDialog}
          onClose={() => setShowDetailsDialog(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>Hızlı İşlemler</DialogTitle>
          <DialogContent>
            <Box sx={{ display: 'grid', gap: 2 }}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<Visibility />}
                onClick={() => {
                  setShowDetailsDialog(false);
                  // Handle view
                }}
                sx={{ justifyContent: 'flex-start', p: 2 }}
              >
                Faturayı Görüntüle
              </Button>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<Download />}
                onClick={() => {
                  setShowDetailsDialog(false);
                  // Handle download
                }}
                sx={{ justifyContent: 'flex-start', p: 2 }}
              >
                PDF İndir
              </Button>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<Print />}
                onClick={() => {
                  setShowDetailsDialog(false);
                  // Handle print
                }}
                sx={{ justifyContent: 'flex-start', p: 2 }}
              >
                Yazdır
              </Button>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<Share />}
                onClick={() => {
                  setShowDetailsDialog(false);
                  // Handle share
                }}
                sx={{ justifyContent: 'flex-start', p: 2 }}
              >
                Paylaş
              </Button>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowDetailsDialog(false)}>İptal</Button>
          </DialogActions>
        </Dialog>
      </Container>
    </Box>
  );
};

export default BillsPage;
