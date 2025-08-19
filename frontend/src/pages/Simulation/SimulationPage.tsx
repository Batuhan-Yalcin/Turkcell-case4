import React, { useState } from 'react';
import {
  Container,
  Typography,
  Card,
  CardContent,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Alert,
  CircularProgress,
  Divider
} from '@mui/material';
import { useMutation, useQuery } from '@tanstack/react-query';
import apiService from '../../services/api';
import { SimulationRequest } from '../../types';

interface SimulationScenario {
  id: number;
  name: string;
  description: string;
  planChange?: string;
  addOnChanges?: string[];
  vasChanges?: string[];
  estimatedCost: number;
  potentialSavings: number;
}

const SimulationPage: React.FC = () => {
  const [selectedScenario, setSelectedScenario] = useState<string>('');
  const [customPlanId, setCustomPlanId] = useState<number | ''>('');
  const [customAddOns, setCustomAddOns] = useState<number[]>([]);
  const [customVAS, setCustomVAS] = useState<number[]>([]);

  const { data: scenarios, isLoading: scenariosLoading } = useQuery({
    queryKey: ['simulation-scenarios'],
    queryFn: () => apiService.getSimulationScenarios().then((res: any) => res.data)
  });

  const { data: plans } = useQuery({
    queryKey: ['plans'],
    queryFn: () => apiService.getPlans().then((res: any) => res.data)
  });

  const { data: addOns } = useQuery({
    queryKey: ['addons'],
    queryFn: () => apiService.getAddOns().then((res: any) => res.data)
  });

  const { data: vasServices } = useQuery({
    queryKey: ['vas'],
    queryFn: () => apiService.getVAS().then((res: any) => res.data)
  });

  const simulationMutation = useMutation({
    mutationFn: (request: SimulationRequest) => 
      apiService.runSimulation(request).then((res: any) => res.data),
    onSuccess: (data) => {
      console.log('Simulation result:', data);
    }
  });

  const handleScenarioSelect = (scenarioType: string) => {
    setSelectedScenario(scenarioType);
  };

  const handleRunSimulation = () => {
    const request: SimulationRequest = {
      userId: 1, // TODO: Get from auth context
      period: '2025-02',
      scenario: {
        planId: customPlanId || undefined,
        addons: customAddOns.length > 0 ? customAddOns : undefined,
        disableVas: customVAS.length === 0,
        blockPremiumSms: false
      }
    };

    simulationMutation.mutate(request);
  };

  if (scenariosLoading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, textAlign: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  // Mock scenarios data since backend doesn't return array
  const mockScenarios: SimulationScenario[] = [
    {
      id: 1,
      name: 'Temel Plan Değişikliği',
      description: 'Mevcut planınızı daha uygun bir planla değiştirin',
      estimatedCost: 89.99,
      potentialSavings: 15.00
    },
    {
      id: 2,
      name: 'Ek Paket Optimizasyonu',
      description: 'Gereksiz ek paketleri kaldırın',
      estimatedCost: 75.50,
      potentialSavings: 25.00
    },
    {
      id: 3,
      name: 'VAS Hizmet Temizliği',
      description: 'Kullanmadığınız VAS hizmetlerini iptal edin',
      estimatedCost: 65.00,
      potentialSavings: 35.00
    }
  ];

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Fatura Simülasyonu
      </Typography>
      
      <Typography variant="body1" color="text.secondary" paragraph>
        Farklı plan ve paket kombinasyonlarını test ederek potansiyel tasarruflarınızı görün.
      </Typography>

      <Box sx={{ 
        display: 'grid', 
        gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
        gap: 3 
      }}>
        {/* Hazır Senaryolar */}
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Hazır Senaryolar
            </Typography>
            <Box sx={{ mb: 2 }}>
              {mockScenarios.map((scenario: SimulationScenario) => (
                <Button
                  key={scenario.id}
                  variant={selectedScenario === scenario.name ? "contained" : "outlined"}
                  fullWidth
                  sx={{ mb: 1, justifyContent: 'flex-start' }}
                  onClick={() => handleScenarioSelect(scenario.name)}
                >
                  <Box sx={{ textAlign: 'left', width: '100%' }}>
                    <Typography variant="subtitle2">{scenario.name}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      Tahmini maliyet: {scenario.estimatedCost} TL
                    </Typography>
                  </Box>
                </Button>
              ))}
            </Box>
          </CardContent>
        </Card>

        {/* Özel Senaryo */}
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Özel Senaryo
            </Typography>
            
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Plan Seçin</InputLabel>
              <Select
                value={customPlanId}
                label="Plan Seçin"
                onChange={(e) => setCustomPlanId(e.target.value as number)}
              >
                {plans?.map((plan: any) => (
                  <MenuItem key={plan.id} value={plan.id}>
                    {plan.name} - {plan.monthlyFee} TL
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Ek Paketler</InputLabel>
              <Select
                multiple
                value={customAddOns}
                label="Ek Paketler"
                onChange={(e) => setCustomAddOns(e.target.value as number[])}
              >
                {addOns?.map((addon: any) => (
                  <MenuItem key={addon.id} value={addon.id}>
                    {addon.name} - {addon.monthlyFee} TL
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>VAS Hizmetleri</InputLabel>
              <Select
                multiple
                value={customVAS}
                label="VAS Hizmetleri"
                onChange={(e) => setCustomVAS(e.target.value as number[])}
              >
                {vasServices?.map((vas: any) => (
                  <MenuItem key={vas.id} value={vas.id}>
                    {vas.name} - {vas.monthlyFee} TL
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <Button
              variant="contained"
              fullWidth
              onClick={handleRunSimulation}
              disabled={simulationMutation.isPending}
            >
              {simulationMutation.isPending ? <CircularProgress size={24} /> : 'Simülasyonu Çalıştır'}
            </Button>
          </CardContent>
        </Card>
      </Box>

      {/* Simülasyon Sonuçları */}
      {simulationMutation.data && (
        <Box sx={{ mt: 3 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Simülasyon Sonucu
              </Typography>
              
              <Box sx={{ 
                display: 'grid', 
                gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
                gap: 2 
              }}>
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
                  <Typography variant="h4" color="primary">
                    {(simulationMutation.data as any).newTotal} TL
                  </Typography>
                  <Typography variant="body2">Yeni Tutar</Typography>
                </Box>
                
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'success.100', borderRadius: 1 }}>
                  <Typography variant="h4" color="success.main">
                    {(simulationMutation.data as any).saving} TL
                  </Typography>
                  <Typography variant="body2">Tasarruf</Typography>
                </Box>
                
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'info.100', borderRadius: 1 }}>
                  <Typography variant="h4" color="info.main">
                    %{((simulationMutation.data as any).saving / (simulationMutation.data as any).currentTotal * 100).toFixed(1)}
                  </Typography>
                  <Typography variant="body2">Tasarruf Oranı</Typography>
                </Box>
              </Box>

              <Divider sx={{ my: 2 }} />
              
              <Typography variant="subtitle1" gutterBottom>
                Öneriler:
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {(simulationMutation.data as any).recommendations?.join(', ')}
              </Typography>
            </CardContent>
          </Card>
        </Box>
      )}

      {simulationMutation.error && (
        <Box sx={{ mt: 3 }}>
          <Alert severity="error">
            Simülasyon çalıştırılırken hata oluştu: {(simulationMutation.error as any).message}
          </Alert>
        </Box>
      )}
    </Container>
  );
};

export default SimulationPage;
