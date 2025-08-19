import React, { useState } from 'react';
import {
  Container,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Stepper,
  Step,
  StepLabel,
  Alert,
  CircularProgress,
  Divider
} from '@mui/material';
import { useMutation, useQuery } from '@tanstack/react-query';
import apiService from '../../services/api';
import { CheckoutRequest, CheckoutAction } from '../../types';

interface CheckoutItem {
  id: number;
  name: string;
  description: string;
  price: number;
  quantity: number;
  type: string;
}

const steps = ['Sepet', 'Ödeme', 'Onay'];

const CheckoutPage: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [paymentMethod, setPaymentMethod] = useState('');
  const [billingAddress, setBillingAddress] = useState('');

  const { data: cartItems, isLoading } = useQuery({
    queryKey: ['checkout-cart'],
    queryFn: () => apiService.getCartItems().then((res: any) => res.data)
  });

  const checkoutMutation = useMutation({
    mutationFn: (request: CheckoutRequest) => 
      apiService.processCheckout(request).then((res: any) => res.data),
    onSuccess: (data) => {
      console.log('Checkout successful:', data);
      setActiveStep(2);
    }
  });

  const handleNext = () => {
    if (activeStep === 0) {
      setActiveStep(1);
    } else if (activeStep === 1) {
      const actions: CheckoutAction[] = [
        {
          type: 'CHANGE_PLAN',
          payload: { planId: 1 }
        }
      ];
      
      const request: CheckoutRequest = {
        userId: 1, // TODO: Get from auth context
        actions
      };
      checkoutMutation.mutate(request);
    }
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const totalAmount = cartItems?.reduce((sum: number, item: CheckoutItem) => sum + (item.price * item.quantity), 0) || 0;

  if (isLoading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, textAlign: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Ödeme Sayfası
      </Typography>

      <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      {activeStep === 0 && (
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
          gap: 3 
        }}>
          <Box sx={{ gridColumn: { xs: '1', md: '1 / 3' } }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Sepet İçeriği
                </Typography>
                {cartItems?.map((item: CheckoutItem) => (
                  <Box key={item.id} sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                    <Box>
                      <Typography variant="subtitle1">{item.name}</Typography>
                      <Typography variant="body2" color="text.secondary">{item.description}</Typography>
                    </Box>
                    <Box sx={{ textAlign: 'right' }}>
                      <Typography variant="subtitle1">₺{item.price}</Typography>
                      <Typography variant="body2" color="text.secondary">Adet: {item.quantity}</Typography>
                    </Box>
                  </Box>
                ))}
              </CardContent>
            </Card>
          </Box>

          <Box>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Toplam
                </Typography>
                <Typography variant="h4" color="primary" gutterBottom>
                  ₺{totalAmount}
                </Typography>
                <Button
                  variant="contained"
                  fullWidth
                  onClick={handleNext}
                  sx={{ mt: 2 }}
                >
                  Devam Et
                </Button>
              </CardContent>
            </Card>
          </Box>
        </Box>
      )}

      {activeStep === 1 && (
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
          gap: 3 
        }}>
          <Box sx={{ gridColumn: { xs: '1', md: '1 / 3' } }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Ödeme Bilgileri
                </Typography>
                
                <Box sx={{ display: 'grid', gap: 2 }}>
                  <FormControl fullWidth>
                    <InputLabel>Ödeme Yöntemi</InputLabel>
                    <Select
                      value={paymentMethod}
                      label="Ödeme Yöntemi"
                      onChange={(e) => setPaymentMethod(e.target.value)}
                    >
                      <MenuItem value="credit_card">Kredi Kartı</MenuItem>
                      <MenuItem value="bank_transfer">Banka Transferi</MenuItem>
                      <MenuItem value="mobile_payment">Mobil Ödeme</MenuItem>
                    </Select>
                  </FormControl>
                  
                  <TextField
                    fullWidth
                    label="Fatura Adresi"
                    multiline
                    rows={3}
                    value={billingAddress}
                    onChange={(e) => setBillingAddress(e.target.value)}
                  />
                </Box>
              </CardContent>
            </Card>
          </Box>

          <Box>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Özet
                </Typography>
                <Typography variant="body1" gutterBottom>
                  Toplam Tutar: ₺{totalAmount}
                </Typography>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Ödeme Yöntemi: {paymentMethod || 'Seçilmedi'}
                </Typography>
                <Button
                  variant="contained"
                  fullWidth
                  onClick={handleNext}
                  disabled={!paymentMethod || !billingAddress}
                  sx={{ mt: 2 }}
                >
                  Ödemeyi Tamamla
                </Button>
                <Button
                  variant="outlined"
                  fullWidth
                  onClick={handleBack}
                  sx={{ mt: 1 }}
                >
                  Geri
                </Button>
              </CardContent>
            </Card>
          </Box>
        </Box>
      )}

      {activeStep === 2 && (
        <Box sx={{ textAlign: 'center' }}>
          <Card>
            <CardContent>
              <Typography variant="h4" color="success.main" gutterBottom>
                Ödeme Başarılı!
              </Typography>
              <Typography variant="body1" paragraph>
                Siparişiniz başarıyla işlendi. Sipariş numaranız: {(checkoutMutation.data as any)?.orderId}
              </Typography>
              <Button
                variant="contained"
                onClick={() => setActiveStep(0)}
                sx={{ mr: 2 }}
              >
                Yeni Sipariş
              </Button>
              <Button
                variant="outlined"
                onClick={() => window.location.href = '/dashboard'}
              >
                Ana Sayfaya Dön
              </Button>
            </CardContent>
          </Card>
        </Box>
      )}

      {checkoutMutation.error && (
        <Box sx={{ mt: 3 }}>
          <Alert severity="error">
            Ödeme işlemi sırasında hata oluştu: {(checkoutMutation.error as any).message}
          </Alert>
        </Box>
      )}
    </Container>
  );
};

export default CheckoutPage;
