import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  Box,
  Typography,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  Dashboard,
  Receipt,
  Store,
  Analytics,
  Warning,
  AccountCircle,
  Settings,
  Help,
  Assessment,
  CompareArrows,
  ShoppingCart,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

const menuItems = [
  {
    text: 'Dashboard',
    icon: <Dashboard />,
    path: '/',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
  {
    text: 'Faturalarım',
    icon: <Receipt />,
    path: '/bills',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
  {
    text: 'AI Fatura Analizi',
    icon: <Analytics />,
    path: '/explain',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
  {
    text: 'Anomali Tespiti',
    icon: <Warning />,
    path: '/anomalies',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
  {
    text: 'Hizmet Kataloğu',
    icon: <Store />,
    path: '/catalog',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
  {
    text: 'Plan Simülasyonu',
    icon: <CompareArrows />,
    path: '/simulation',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
  {
    text: 'Sipariş Yönetimi',
    icon: <ShoppingCart />,
    path: '/checkout',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
  {
    text: 'Kullanım Analizi',
    icon: <Analytics />,
    path: '/usage',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
  {
    text: 'Raporlar',
    icon: <Assessment />,
    path: '/reports',
    roles: ['ROLE_ADMIN'],
  },
  {
    text: 'Kullanıcı Yönetimi',
    icon: <AccountCircle />,
    path: '/users',
    roles: ['ROLE_ADMIN'],
  },
  {
    text: 'Ayarlar',
    icon: <Settings />,
    path: '/settings',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
  {
    text: 'Yardım',
    icon: <Help />,
    path: '/help',
    roles: ['ROLE_USER', 'ROLE_ADMIN'],
  },
];

const Sidebar: React.FC<SidebarProps> = ({ open, onClose }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();

  const drawerWidth = 280;

  const handleNavigation = (path: string) => {
    navigate(path);
    if (isMobile) {
      onClose();
    }
  };

  const filteredMenuItems = menuItems.filter(item =>
    item.roles.includes(user?.role || 'ROLE_USER')
  );

  const drawerContent = (
    <Box sx={{ width: drawerWidth }}>
      <Box
        sx={{
          p: 3,
          background: 'linear-gradient(135deg, #00A3E0 0%, #0077A3 100%)',
          color: 'white',
          textAlign: 'center',
        }}
      >
        <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
          Turkcell
        </Typography>
        <Typography variant="body2" sx={{ opacity: 0.9 }}>
          Fatura Asistanı
        </Typography>
      </Box>

      <List sx={{ pt: 2 }}>
        {filteredMenuItems.map((item, index) => {
          const isActive = location.pathname === item.path;
          
          return (
            <ListItem key={item.text} disablePadding>
              <ListItemButton
                onClick={() => handleNavigation(item.path)}
                sx={{
                  mx: 2,
                  mb: 0.5,
                  borderRadius: 2,
                  backgroundColor: isActive ? 'rgba(0, 163, 224, 0.1)' : 'transparent',
                  color: isActive ? theme.palette.primary.main : 'inherit',
                  '&:hover': {
                    backgroundColor: isActive 
                      ? 'rgba(0, 163, 224, 0.15)' 
                      : 'rgba(0, 0, 0, 0.04)',
                  },
                  '& .MuiListItemIcon-root': {
                    color: isActive ? theme.palette.primary.main : 'inherit',
                  },
                }}
              >
                <ListItemIcon sx={{ minWidth: 40 }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText 
                  primary={item.text}
                  primaryTypographyProps={{
                    fontWeight: isActive ? 600 : 400,
                  }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      <Box sx={{ mt: 'auto', p: 2 }}>
        <Divider sx={{ mb: 2 }} />
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="caption" sx={{ color: 'text.secondary' }}>
            v1.0.0
          </Typography>
        </Box>
      </Box>
    </Box>
  );

  return (
    <>
      {/* Mobile Drawer */}
      {isMobile && (
        <Drawer
          variant="temporary"
          open={open}
          onClose={onClose}
          ModalProps={{
            keepMounted: true,
          }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
            },
          }}
        >
          {drawerContent}
        </Drawer>
      )}

      {/* Desktop Drawer */}
      {!isMobile && (
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
              borderRight: '1px solid rgba(0, 0, 0, 0.12)',
            },
          }}
          open
        >
          {drawerContent}
        </Drawer>
      )}
    </>
  );
};

export default Sidebar;
