import React, { useState } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
  Link,
  InputAdornment,
  useTheme,
  Fade,
  Grow,
  Divider,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormHelperText,
  Stepper,
  Step,
  StepLabel,
  StepContent,
} from '@mui/material';
import {
  Person,
  Phone,
  Business,
  Email,
  LocationOn,
  Security,
  CheckCircle,
  ArrowForward,
  ArrowBack,
} from '@mui/icons-material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { RegisterRequest } from '../../types';

const RegisterPage: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [formData, setFormData] = useState<RegisterRequest>({
    name: '',
    msisdn: '',
    userType: 'INDIVIDUAL',
    currentPlanId: undefined,
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();

  const steps = [
    {
      label: 'Kişisel Bilgiler',
      description: 'Adınız ve iletişim bilgilerinizi girin',
      icon: <Person />,
    },
    {
      label: 'Hesap Türü',
      description: 'Bireysel veya kurumsal hesap seçin',
      icon: <Business />,
    },
    {
      label: 'Doğrulama',
      description: 'Bilgilerinizi kontrol edin ve kayıt olun',
      icon: <Security />,
    },
  ];

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleInputChange = (field: keyof RegisterRequest, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async () => {
    try {
      setIsLoading(true);
      setError('');
      await register(formData);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Kayıt olurken bir hata oluştu');
    } finally {
      setIsLoading(false);
    }
  };

  const isStepValid = (step: number) => {
    switch (step) {
      case 0:
        return formData.name.trim() && formData.msisdn.trim();
      case 1:
        return formData.userType;
      case 2:
        return true;
      default:
        return false;
    }
  };

  const benefits = [
    { icon: <CheckCircle />, title: 'Ücretsiz Kayıt', desc: 'Hiçbir ücret ödemeden hesap oluşturun' },
    { icon: <CheckCircle />, title: 'Anında Erişim', desc: 'Kayıt sonrası hemen sisteme giriş yapın' },
    { icon: <CheckCircle />, title: 'Güvenli Altyapı', desc: 'En üst düzey güvenlik standartları' },
    { icon: <CheckCircle />, title: '7/24 Destek', desc: 'Kesintisiz müşteri hizmetleri' },
  ];

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
        display: 'flex',
        alignItems: 'center',
        py: 4,
      }}
    >
      <Container maxWidth="lg">
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' }, gap: 4 }}>
          
          {/* Sol Taraf - Kayıt Formu */}
          <Grow in={true} timeout={800}>
            <Paper
              elevation={24}
              sx={{
                p: { xs: 3, md: 5 },
                borderRadius: 4,
                background: 'rgba(255, 255, 255, 0.95)',
                backdropFilter: 'blur(20px)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                boxShadow: '0px 20px 60px rgba(0, 0, 0, 0.1)',
              }}
            >
              {/* Logo ve Başlık */}
              <Box sx={{ textAlign: 'center', mb: 4 }}>
                <Box
                  component="img"
                  src="/logo.png"
                  alt="Turkcell Logo"
                  sx={{
                    height: 80,
                    mb: 2,
                    filter: 'drop-shadow(0px 4px 8px rgba(0,0,0,0.1))',
                  }}
                />
                <Typography
                  variant="h3"
                  sx={{
                    fontWeight: 800,
                    mb: 1,
                    background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                    backgroundClip: 'text',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                  }}
                >
                  Turkcell
                </Typography>
                <Typography
                  variant="h5"
                  sx={{
                    fontWeight: 600,
                    color: 'text.secondary',
                    mb: 2,
                  }}
                >
                  Hesap Oluştur
                </Typography>
                <Typography
                  variant="body1"
                  sx={{
                    color: 'text.secondary',
                    opacity: 0.8,
                    maxWidth: 400,
                    mx: 'auto',
                  }}
                >
                  Hemen ücretsiz hesap oluşturun ve faturalarınızı yönetmeye başlayın
                </Typography>
              </Box>

              {/* Hata Mesajı */}
              {error && (
                <Fade in={!!error}>
                  <Alert
                    severity="error"
                    sx={{
                      mb: 3,
                      borderRadius: 2,
                      '& .MuiAlert-icon': { fontSize: 24 },
                    }}
                    variant="filled"
                  >
                    {error}
                  </Alert>
                </Fade>
              )}

              {/* Stepper */}
              <Stepper activeStep={activeStep} orientation="vertical" sx={{ mb: 4 }}>
                {steps.map((step, index) => (
                  <Step key={step.label}>
                    <StepLabel
                      StepIconComponent={() => (
                        <Box
                          sx={{
                            width: 32,
                            height: 32,
                            borderRadius: '50%',
                            bgcolor: activeStep >= index ? 'primary.main' : 'grey.300',
                            color: activeStep >= index ? 'white' : 'grey.600',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: 16,
                          }}
                        >
                          {step.icon}
                        </Box>
                      )}
                    >
                      <Typography variant="h6" sx={{ fontWeight: 600 }}>
                        {step.label}
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                        {step.description}
                      </Typography>
                    </StepLabel>
                    <StepContent>
                      <Box sx={{ mt: 2 }}>
                        {/* Step 0: Kişisel Bilgiler */}
                        {index === 0 && (
                          <Box sx={{ display: 'grid', gap: 3 }}>
                            <TextField
                              fullWidth
                              label="Ad Soyad"
                              variant="outlined"
                              value={formData.name}
                              onChange={(e) => handleInputChange('name', e.target.value)}
                              placeholder="Adınız ve soyadınız"
                              InputProps={{
                                startAdornment: (
                                  <InputAdornment position="start">
                                    <Person sx={{ color: 'primary.main' }} />
                                  </InputAdornment>
                                ),
                                sx: { borderRadius: 3 },
                              }}
                            />
                            <TextField
                              fullWidth
                              label="MSISDN"
                              variant="outlined"
                              value={formData.msisdn}
                              onChange={(e) => handleInputChange('msisdn', e.target.value)}
                              placeholder="5XX XXX XX XX"
                              InputProps={{
                                startAdornment: (
                                  <InputAdornment position="start">
                                    <Phone sx={{ color: 'primary.main' }} />
                                  </InputAdornment>
                                ),
                                sx: { borderRadius: 3 },
                              }}
                            />
                          </Box>
                        )}

                        {/* Step 1: Hesap Türü */}
                        {index === 1 && (
                          <Box sx={{ display: 'grid', gap: 3 }}>
                            <FormControl fullWidth>
                              <InputLabel>Hesap Türü</InputLabel>
                              <Select
                                value={formData.userType}
                                label="Hesap Türü"
                                onChange={(e) => handleInputChange('userType', e.target.value)}
                                sx={{ borderRadius: 3 }}
                              >
                                <MenuItem value="INDIVIDUAL">Bireysel Hesap</MenuItem>
                                <MenuItem value="CORPORATE">Kurumsal Hesap</MenuItem>
                              </Select>
                            </FormControl>
                            {formData.userType === 'CORPORATE' && (
                              <TextField
                                fullWidth
                                label="Plan ID (Opsiyonel)"
                                variant="outlined"
                                type="number"
                                value={formData.currentPlanId || ''}
                                onChange={(e) => handleInputChange('currentPlanId', e.target.value ? parseInt(e.target.value) : undefined)}
                                placeholder="Mevcut plan ID'niz"
                                InputProps={{
                                  startAdornment: (
                                    <InputAdornment position="start">
                                      <Business sx={{ color: 'primary.main' }} />
                                    </InputAdornment>
                                  ),
                                  sx: { borderRadius: 3 },
                                }}
                              />
                            )}
                          </Box>
                        )}

                        {/* Step 2: Doğrulama */}
                        {index === 2 && (
                          <Box sx={{ p: 2, bgcolor: 'grey.50', borderRadius: 3 }}>
                            <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
                              Bilgilerinizi Kontrol Edin
                            </Typography>
                            <Box sx={{ display: 'grid', gap: 1 }}>
                              <Typography variant="body2">
                                <strong>Ad Soyad:</strong> {formData.name}
                              </Typography>
                              <Typography variant="body2">
                                <strong>MSISDN:</strong> {formData.msisdn}
                              </Typography>
                              <Typography variant="body2">
                                <strong>Hesap Türü:</strong> {formData.userType === 'INDIVIDUAL' ? 'Bireysel' : 'Kurumsal'}
                              </Typography>
                              {formData.currentPlanId && (
                                <Typography variant="body2">
                                  <strong>Plan ID:</strong> {formData.currentPlanId}
                                </Typography>
                              )}
                            </Box>
                          </Box>
                        )}

                        {/* Navigation Buttons */}
                        <Box sx={{ mt: 3, display: 'flex', gap: 1 }}>
                          <Button
                            disabled={index === 0}
                            onClick={handleBack}
                            startIcon={<ArrowBack />}
                            variant="outlined"
                            sx={{ borderRadius: 3 }}
                          >
                            Geri
                          </Button>
                          <Box sx={{ flex: '1' }} />
                          {index === steps.length - 1 ? (
                                                         <Button
                               variant="contained"
                               onClick={handleSubmit}
                               disabled={isLoading || !isStepValid(index)}
                               endIcon={isLoading ? <CircularProgress size={20} /> : <CheckCircle />}
                               sx={{
                                 borderRadius: 3,
                                 background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                                 '&:hover': {
                                   background: 'linear-gradient(135deg, #0077A3 0%, #005580 100%)',
                                 },
                               }}
                             >
                              {isLoading ? 'Kaydediliyor...' : 'Hesap Oluştur'}
                            </Button>
                          ) : (
                            <Button
                              variant="contained"
                              onClick={handleNext}
                              disabled={!isStepValid(index)}
                              endIcon={<ArrowForward />}
                              sx={{
                                borderRadius: 3,
                                background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                                '&:hover': {
                                  background: 'linear-gradient(135deg, #0077A3 0%, #005580 100%)',
                                },
                              }}
                            >
                              İleri
                            </Button>
                          )}
                        </Box>
                      </Box>
                    </StepContent>
                  </Step>
                ))}
              </Stepper>

              {/* Giriş Yap Linki */}
              <Box sx={{ mt: 4, textAlign: 'center' }}>
                <Divider sx={{ mb: 2 }}>
                  <Chip label="veya" variant="outlined" size="small" />
                </Divider>
                <Typography variant="body1" sx={{ color: 'text.secondary', mb: 1 }}>
                  Zaten hesabınız var mı?
                </Typography>
                <Link
                  component={RouterLink}
                  to="/login"
                  sx={{
                    color: '#E60000',
                    textDecoration: 'none',
                    fontWeight: 600,
                    fontSize: '1.1rem',
                    '&:hover': {
                      textDecoration: 'underline',
                      color: '#CC0000',
                    },
                  }}
                >
                  Giriş Yapın
                </Link>
              </Box>
            </Paper>
          </Grow>

          {/* Sağ Taraf - Avantajlar ve Bilgiler */}
          <Grow in={true} timeout={1200}>
            <Box sx={{ display: { xs: 'none', lg: 'flex' }, flexDirection: 'column', justifyContent: 'center' }}>
              {/* Avantajlar */}
              <Box sx={{ mb: 4 }}>
                <Typography
                  variant="h4"
                  sx={{
                    fontWeight: 700,
                    mb: 3,
                    color: 'text.primary',
                    textAlign: 'center',
                  }}
                >
                  Neden Ücretsiz Kayıt?
                </Typography>
                <Box sx={{ display: 'grid', gap: 2 }}>
                  {benefits.map((benefit, index) => (
                    <Fade in={true} timeout={1600 + index * 200} key={benefit.title}>
                      <Box
                        sx={{
                          display: 'flex',
                          alignItems: 'center',
                          p: 2,
                          borderRadius: 3,
                          bgcolor: 'rgba(0, 163, 224, 0.15)', // Turkcell Mavisi arka plan
                          backdropFilter: 'blur(10px)',
                          border: '1px solid rgba(0, 163, 224, 0.3)',
                          transition: 'all 0.3s ease',
                          '&:hover': {
                            transform: 'translateX(8px)',
                            bgcolor: 'rgba(0, 163, 224, 0.25)',
                            boxShadow: '0px 8px 24px rgba(0, 163, 224, 0.3)',
                          },
                        }}
                      >
                        <Box
                          sx={{
                            p: 1.5,
                            borderRadius: 2,
                            bgcolor: '#00A3E0', // Turkcell Mavisi arka plan
                            color: '#FFD700', // Turkcell Sarısı ikon
                            mr: 2,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            boxShadow: '0px 4px 12px rgba(0, 163, 224, 0.4)',
                          }}
                        >
                          {benefit.icon}
                        </Box>
                        <Box>
                          <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>
                            {benefit.title}
                          </Typography>
                          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                            {benefit.desc}
                          </Typography>
                        </Box>
                      </Box>
                    </Fade>
                  ))}
                </Box>
              </Box>

              {/* Güvenlik Bilgileri */}
              <Box sx={{ textAlign: 'center' }}>
                <Typography
                  variant="h5"
                  sx={{
                    fontWeight: 700,
                    mb: 3,
                    color: 'text.primary',
                  }}
                >
                  Güvenlik ve Gizlilik
                </Typography>
                <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 2 }}>
                  {[
                    { value: 'SSL', label: 'Şifreli Bağlantı' },
                    { value: 'GDPR', label: 'Uyumlu' },
                    { value: 'ISO', label: '27001 Sertifikası' },
                    { value: '24/7', label: 'Güvenlik' },
                  ].map((security, index) => (
                    <Fade in={true} timeout={2000 + index * 200} key={security.label}>
                      <Box
                        sx={{
                          p: 2,
                          borderRadius: 2,
                          bgcolor: 'rgba(0, 163, 224, 0.15)', // Turkcell Mavisi arka plan
                          backdropFilter: 'blur(10px)',
                          border: '1px solid rgba(0, 163, 224, 0.3)',
                        }}
                      >
                        <Typography
                          variant="h4"
                          sx={{
                            fontWeight: 800,
                            color: '#00A3E0', // Turkcell Mavisi
                            mb: 0.5,
                          }}
                        >
                          {security.value}
                        </Typography>
                        <Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                          {security.label}
                        </Typography>
                      </Box>
                    </Fade>
                  ))}
                </Box>
              </Box>
            </Box>
          </Grow>
        </Box>
      </Container>
    </Box>
  );
};

export default RegisterPage;
