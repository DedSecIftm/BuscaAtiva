import { createTheme } from '@mui/material/styles'

const theme = createTheme({
  palette: {
    primary: { main: '#1565c0' },
    background: { default: '#f5f7fa' },
  },
  typography: {
    fontFamily: 'system-ui, "Segoe UI", Roboto, Arial, sans-serif',
    fontSize: 15,
    button: { textTransform: 'none', fontWeight: 600 },
  },
  shape: { borderRadius: 10 },
})

export default theme
