import React, { useState, useEffect } from 'react';
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
  Divider,
  Chip,
  Grid
} from '@mui/material';
import { useMutation, useQuery } from '@tanstack/react-query';
import apiService from '../../services/api';
import { CheckoutRequest, CheckoutAction } from '../../types';
import { useAuth } from '../../contexts/AuthContext';

interface CheckoutItem {
  id: number;
  name: string;
  description: string;
  price: number;
  quantity: number;
  type: string;
}

interface CurrentPlan {
  id: number;
  name: string;
  monthlyFee: number;
  dataLimit: number;
  voiceLimit: number;
  smsLimit: number;
}

interface CurrentVAS {
  id: number;
  name: string;
  monthlyFee: number;
  category: string;
}

const steps = ['Mevcut Hizmetler', 'Değişiklikler', 'Onay'];

const CheckoutPage: React.FC = () => {
  const { user } = useAuth();
  const [activeStep, setActiveStep] = useState(0);
  const [selectedChanges, setSelectedChanges] = useState<string[]>([]);
  const [newPlanId, setNewPlanId] = useState<number | ''>('');
  const [selectedAddOns, setSelectedAddOns] = useState<number[]>([]);
  const [selectedVAS, setSelectedVAS] = useState<number[]>([]);

  // Get current user's plan and services
  const { data: currentPlan, isLoading: planLoading } = useQuery({
    queryKey: ['current-plan', user?.userId],
    queryFn: () => apiService.getCurrentPlan(user?.userId || 1).then((res: any) => res.data),
    enabled: !!user?.userId
  });

  const { data: currentVAS, isLoading: vasLoading } = useQuery({
    queryKey: ['current-vas', user?.userId],
    queryFn: () => apiService.getCurrentVAS(user?.userId || 1).then((res: any) => res.data),
    enabled: !!user?.userId
  });

  // Get available plans and add-ons for changes
  const { data: availablePlans } = useQuery({
    queryKey: ['available-plans'],
    queryFn: () => apiService.getPlans().then((res: any) => res.data)
  });

  const { data: availableAddOns } = useQuery({
    queryKey: ['available-addons'],
    queryFn: () => apiService.getAddOns().then((res: any) => res.data)
  });

  const { data: availableVAS } = useQuery({
    queryKey: ['available-vas'],
    queryFn: () => apiService.getVAS().then((res: any) => res.data)
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
      const actions: CheckoutAction[] = [];
      
      if (newPlanId) {
        actions.push({
          type: 'CHANGE_PLAN',
          payload: { planId: newPlanId }
        });
      }
      
      if (selectedAddOns.length > 0) {
        actions.push({
          type: 'ADD_ADDON',
          payload: { addonIds: selectedAddOns }
        });
      }
      
      if (selectedVAS.length > 0) {
        actions.push({
          type: 'CANCEL_VAS',
          payload: { vasIds: selectedVAS }
        });
      }
      
      const request: CheckoutRequest = {
        userId: user?.userId || 1,
        actions
      };
      
      if (actions.length > 0) {
        checkoutMutation.mutate(request);
      } else {
        setActiveStep(2);
      }
    }
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const isLoading = planLoading || vasLoading;

  if (isLoading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, textAlign: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  const currentMonthlyCost = (currentPlan?.monthlyFee || 0) + 
    (currentVAS?.reduce((sum: number, vas: CurrentVAS) => sum + vas.monthlyFee, 0) || 0);

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Sipariş Yönetimi
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
          gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
          gap: 3 
        }}>
          {/* Mevcut Plan */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Mevcut Plan
              </Typography>
              {currentPlan ? (
                <Box>
                  <Typography variant="h5" color="primary" gutterBottom>
                    {currentPlan.name}
                  </Typography>
                  <Box sx={{ mb: 2 }}>
                    <Chip label={`${currentPlan.dataLimit} GB`} color="primary" sx={{ mr: 1, mb: 1 }} />
                    <Chip label={`${currentPlan.voiceLimit} dk`} color="secondary" sx={{ mr: 1, mb: 1 }} />
                    <Chip label={`${currentPlan.smsLimit} SMS`} color="success" sx={{ mr: 1, mb: 1 }} />
                  </Box>
                  <Typography variant="h6" color="primary">
                    ₺{currentPlan.monthlyFee}/ay
                  </Typography>
                </Box>
              ) : (
                <Typography color="text.secondary">Plan bulunamadı</Typography>
              )}
            </CardContent>
          </Card>

          {/* Mevcut VAS Hizmetleri */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Mevcut VAS Hizmetleri
              </Typography>
              {currentVAS && currentVAS.length > 0 ? (
                <Box>
                  {currentVAS.map((vas: CurrentVAS) => (
                    <Box key={vas.id} sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                      <Box>
                        <Typography variant="subtitle1">{vas.name}</Typography>
                        <Typography variant="body2" color="text.secondary">{vas.category}</Typography>
                      </Box>
                      <Typography variant="subtitle1" color="primary">
                        ₺{vas.monthlyFee}/ay
                      </Typography>
                    </Box>
                  ))}
                </Box>
              ) : (
                <Typography color="text.secondary">VAS hizmeti bulunmuyor</Typography>
              )}
              
              <Divider sx={{ my: 2 }} />
              <Typography variant="h6" color="primary">
                Toplam Aylık: ₺{currentMonthlyCost}
              </Typography>
            </CardContent>
          </Card>
        </Box>
      )}

      {activeStep === 1 && (
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
          gap: 3 
        }}>
          {/* Plan Değişikliği */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Plan Değişikliği
              </Typography>
              
              <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>Yeni Plan Seçin</InputLabel>
                <Select
                  value={newPlanId}
                  label="Yeni Plan Seçin"
                  onChange={(e) => setNewPlanId(e.target.value as number)}
                >
                  <MenuItem value="">Mevcut planı koru</MenuItem>
                  {availablePlans?.map((plan: any) => (
                    <MenuItem key={plan.id} value={plan.id}>
                      {plan.name} - ₺{plan.monthlyFee}/ay
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>Ek Paketler</InputLabel>
                <Select
                  multiple
                  value={selectedAddOns}
                  label="Ek Paketler"
                  onChange={(e) => setSelectedAddOns(e.target.value as number[])}
                >
                  {availableAddOns?.map((addon: any) => (
                    <MenuItem key={addon.id} value={addon.id}>
                      {addon.name} - ₺{addon.monthlyFee}/ay
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </CardContent>
          </Card>

          {/* VAS Değişiklikleri */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                VAS Hizmet Değişiklikleri
              </Typography>
              
              <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>İptal Edilecek VAS</InputLabel>
                <Select
                  multiple
                  value={selectedVAS}
                  label="İptal Edilecek VAS"
                  onChange={(e) => setSelectedVAS(e.target.value as number[])}
                >
                  {currentVAS?.map((vas: CurrentVAS) => (
                    <MenuItem key={vas.id} value={vas.id}>
                      {vas.name} - ₺{vas.monthlyFee}/ay
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                Seçilen değişiklikler bir sonraki fatura döneminde geçerli olacaktır.
              </Typography>
            </CardContent>
          </Card>
        </Box>
      )}

      {activeStep === 2 && (
        <Box sx={{ textAlign: 'center' }}>
          <Card>
            <CardContent>
              <Typography variant="h4" color="success.main" gutterBottom>
                İşlem Tamamlandı!
              </Typography>
              <Typography variant="body1" paragraph>
                {checkoutMutation.data ? 
                  `Siparişiniz başarıyla işlendi. Sipariş numaranız: ${(checkoutMutation.data as any)?.orderId}` :
                  'Değişiklik talebiniz alındı. Bir sonraki fatura döneminde geçerli olacaktır.'
                }
              </Typography>
              <Button
                variant="contained"
                onClick={() => setActiveStep(0)}
                sx={{ mr: 2 }}
              >
                Yeni Değişiklik
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

      {/* Navigation Buttons */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
        <Button
          disabled={activeStep === 0}
          onClick={handleBack}
        >
          Geri
        </Button>
        <Button
          variant="contained"
          onClick={handleNext}
          disabled={checkoutMutation.isPending}
        >
          {activeStep === steps.length - 1 ? 'Tamamla' : 'Devam Et'}
        </Button>
      </Box>

      {checkoutMutation.error && (
        <Box sx={{ mt: 3 }}>
          <Alert severity="error">
            İşlem sırasında hata oluştu: {(checkoutMutation.error as any).message}
          </Alert>
        </Box>
      )}
    </Container>
  );
};

export default CheckoutPage;
