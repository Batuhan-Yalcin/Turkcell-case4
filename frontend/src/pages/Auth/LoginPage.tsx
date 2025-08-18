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
  IconButton,
  useTheme,
  Fade,
  Grow,
  Divider,
  Chip,
} from '@mui/material';
import {
  Phone,
  Visibility,
  VisibilityOff,
  Business,
  Security,
  Speed,
  TrendingUp,
  CheckCircle,
} from '@mui/icons-material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const LoginPage: React.FC = () => {
  const [msisdn, setMsisdn] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!msisdn.trim()) {
      setError('MSISDN alanı zorunludur');
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      await login({ msisdn: msisdn.trim() });
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Giriş yapılırken bir hata oluştu');
    } finally {
      setIsLoading(false);
    }
  };

  const features = [
    { icon: <Business />, title: 'Kurumsal Çözümler', desc: 'İşletmeniz için özel paketler' },
    { icon: <Security />, title: 'Güvenli Altyapı', desc: 'En üst düzey güvenlik standartları' },
    { icon: <Speed />, title: 'Hızlı Hizmet', desc: '7/24 kesintisiz destek' },
    { icon: <TrendingUp />, title: 'Sürekli Gelişim', desc: 'Yenilikçi teknolojiler' },
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
          
          {/* Sol Taraf - Giriş Formu */}
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
                  Fatura Asistanı
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
                  Faturalarınızı akıllıca yönetin, tasarruf edin
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

              {/* Giriş Formu */}
              <Box component="form" onSubmit={handleSubmit}>
                <TextField
                  fullWidth
                  label="MSISDN"
                  variant="outlined"
                  value={msisdn}
                  onChange={(e) => setMsisdn(e.target.value)}
                  placeholder="5XX XXX XX XX"
                  sx={{ mb: 3 }}
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <Phone sx={{ color: 'primary.main' }} />
                      </InputAdornment>
                    ),
                    sx: {
                      borderRadius: 3,
                      '& .MuiOutlinedInput-notchedOutline': {
                        borderColor: 'rgba(0,0,0,0.1)',
                        borderWidth: 2,
                      },
                      '&:hover .MuiOutlinedInput-notchedOutline': {
                        borderColor: 'rgba(230,0,0,0.3)',
                      },
                      '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                        borderColor: '#E60000',
                        borderWidth: 2,
                      },
                    },
                  }}
                  InputLabelProps={{
                    sx: {
                      '&.Mui-focused': {
                        color: '#E60000',
                      },
                    },
                  }}
                />

                                 <Button
                   type="submit"
                   fullWidth
                   variant="contained"
                   disabled={isLoading}
                   sx={{
                     py: 2,
                     borderRadius: 3,
                     fontSize: '1.1rem',
                     fontWeight: 600,
                     background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
                     boxShadow: '0px 8px 32px rgba(0, 163, 224, 0.3)',
                     '&:hover': {
                       background: 'linear-gradient(135deg, #0077A3 0%, #005580 100%)',
                       transform: 'translateY(-2px)',
                       boxShadow: '0px 12px 40px rgba(0, 163, 224, 0.4)',
                     },
                     transition: 'all 0.3s ease',
                   }}
                 >
                  {isLoading ? (
                    <CircularProgress size={24} color="inherit" />
                  ) : (
                    'Giriş Yap'
                  )}
                </Button>

                {/* Demo Bilgisi */}
                <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 2, textAlign: 'center' }}>
                  <Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                    <strong>Demo Giriş:</strong> Herhangi bir MSISDN numarası ile giriş yapabilirsiniz
                  </Typography>
                </Box>

                {/* Kayıt Ol Linki */}
                <Box sx={{ mt: 4, textAlign: 'center' }}>
                  <Divider sx={{ mb: 2 }}>
                    <Chip label="veya" variant="outlined" size="small" />
                  </Divider>
                  <Typography variant="body1" sx={{ color: 'text.secondary', mb: 1 }}>
                    Hesabınız yok mu?
                  </Typography>
                  <Link
                    component={RouterLink}
                    to="/register"
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
                    Hemen Kayıt Olun
                  </Link>
                </Box>
              </Box>
            </Paper>
          </Grow>

          {/* Sağ Taraf - Özellikler ve Bilgiler */}
          <Grow in={true} timeout={1200}>
            <Box sx={{ display: { xs: 'none', lg: 'flex' }, flexDirection: 'column', justifyContent: 'center' }}>
              {/* Özellikler */}
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
                  Neden Turkcell?
                </Typography>
                <Box sx={{ display: 'grid', gap: 2 }}>
                  {features.map((feature, index) => (
                    <Fade in={true} timeout={1600 + index * 200} key={feature.title}>
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
                           {feature.icon}
                         </Box>
                        <Box>
                          <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>
                            {feature.title}
                          </Typography>
                          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                            {feature.desc}
                          </Typography>
                        </Box>
                      </Box>
                    </Fade>
                  ))}
                </Box>
              </Box>

              {/* İstatistikler */}
              <Box sx={{ textAlign: 'center' }}>
                <Typography
                  variant="h5"
                  sx={{
                    fontWeight: 700,
                    mb: 3,
                    color: 'text.primary',
                  }}
                >
                  Güvenilir Altyapı
                </Typography>
                <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 2 }}>
                  {[
                    { value: '99.9%', label: 'Uptime' },
                    { value: '25+', label: 'Yıl Deneyim' },
                    { value: '40M+', label: 'Müşteri' },
                    { value: '7/24', label: 'Destek' },
                  ].map((stat, index) => (
                    <Fade in={true} timeout={2000 + index * 200} key={stat.label}>
                      <Box
                        sx={{
                          p: 2,
                          borderRadius: 2,
                          bgcolor: 'rgba(255, 255, 255, 0.7)',
                          backdropFilter: 'blur(10px)',
                          border: '1px solid rgba(255, 255, 255, 0.3)',
                        }}
                      >
                        <Typography
                          variant="h4"
                          sx={{
                            fontWeight: 800,
                            color: 'primary.main',
                            mb: 0.5,
                          }}
                        >
                          {stat.value}
                        </Typography>
                        <Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                          {stat.label}
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

export default LoginPage;
