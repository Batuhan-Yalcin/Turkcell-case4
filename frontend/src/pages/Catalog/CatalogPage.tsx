import React, { useState } from 'react';
import {
  Container,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  Box,
  Tabs,
  Tab,
  CircularProgress
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import apiService from '../../services/api';

interface Plan {
  id: number;
  name: string;
  description: string;
  monthlyFee: number;
  dataLimit: number;
  voiceLimit: number;
  smsLimit: number;
  type: string;
}

interface AddOnPack {
  id: number;
  name: string;
  description: string;
  monthlyFee: number;
  type: string;
  features: string[];
}

interface VAS {
  id: number;
  name: string;
  description: string;
  monthlyFee: number;
  category: string;
}

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
      id={`catalog-tabpanel-${index}`}
      aria-labelledby={`catalog-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const CatalogPage: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);

  const { data: plans, isLoading: plansLoading } = useQuery({
    queryKey: ['plans'],
    queryFn: () => apiService.getPlans().then((res: any) => res.data)
  });

  const { data: addOns, isLoading: addOnsLoading } = useQuery({
    queryKey: ['addons'],
    queryFn: () => apiService.getAddOns().then((res: any) => res.data)
  });

  const { data: vasServices, isLoading: vasLoading } = useQuery({
    queryKey: ['vas'],
    queryFn: () => apiService.getVAS().then((res: any) => res.data)
  });

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const isLoading = plansLoading || addOnsLoading || vasLoading;

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
        Hizmet Kataloğu
      </Typography>
      
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="catalog tabs">
          <Tab label="Tarifeler" />
          <Tab label="Ek Paketler" />
          <Tab label="VAS Hizmetleri" />
        </Tabs>
      </Box>

      <TabPanel value={tabValue} index={0}>
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' },
          gap: 3 
        }}>
          {plans?.map((plan: Plan) => (
            <Card key={plan.id}>
              <CardContent>
                <Typography variant="h6" component="h2" gutterBottom>
                  {plan.name}
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  {plan.description}
                </Typography>
                <Box sx={{ mb: 2 }}>
                  <Chip 
                    label={`${plan.dataLimit} GB`} 
                    color="primary" 
                    size="small" 
                    sx={{ mr: 1, mb: 1 }}
                  />
                  <Chip 
                    label={`${plan.voiceLimit} dk`} 
                    color="secondary" 
                    size="small" 
                    sx={{ mr: 1, mb: 1 }}
                  />
                  <Chip 
                    label={`${plan.smsLimit} SMS`} 
                    color="success" 
                    size="small" 
                    sx={{ mr: 1, mb: 1 }}
                  />
                </Box>
                <Typography variant="h5" color="primary">
                  {plan.monthlyFee} TL/ay
                </Typography>
              </CardContent>
              <CardActions>
                <Button size="small" color="primary">
                  Detaylar
                </Button>
                <Button size="small" variant="contained">
                  Seç
                </Button>
              </CardActions>
            </Card>
          ))}
        </Box>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' },
          gap: 3 
        }}>
          {addOns?.map((addon: AddOnPack) => (
            <Card key={addon.id}>
              <CardContent>
                <Typography variant="h6" component="h2" gutterBottom>
                  {addon.name}
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  {addon.description}
                </Typography>
                <Box sx={{ mb: 2 }}>
                  {addon.features?.map((feature, index) => (
                    <Chip 
                      key={index}
                      label={feature} 
                      variant="outlined" 
                      size="small" 
                      sx={{ mr: 1, mb: 1 }}
                    />
                  ))}
                </Box>
                <Typography variant="h5" color="primary">
                  {addon.monthlyFee} TL/ay
                </Typography>
              </CardContent>
              <CardActions>
                <Button size="small" color="primary">
                  Detaylar
                </Button>
                <Button size="small" variant="contained">
                  Ekle
                </Button>
              </CardActions>
            </Card>
          ))}
        </Box>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' },
          gap: 3 
        }}>
          {vasServices?.map((vas: VAS) => (
            <Card key={vas.id}>
              <CardContent>
                <Typography variant="h6" component="h2" gutterBottom>
                  {vas.name}
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  {vas.description}
                </Typography>
                <Chip 
                  label={vas.category} 
                  color="info" 
                  size="small" 
                  sx={{ mb: 2 }}
                />
                <Typography variant="h5" color="primary">
                  {vas.monthlyFee} TL/ay
                </Typography>
              </CardContent>
              <CardActions>
                <Button size="small" color="primary">
                  Detaylar
                </Button>
                <Button size="small" variant="contained">
                  Aktifleştir
                </Button>
              </CardActions>
            </Card>
          ))}
        </Box>
      </TabPanel>
    </Container>
  );
};

export default CatalogPage;
