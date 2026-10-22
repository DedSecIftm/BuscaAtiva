import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import useMediaQuery from '@mui/material/useMediaQuery'
import { useTheme } from '@mui/material/styles'
import type { Tabela } from '../importacao/lerArquivo.ts'

const LIMITE_LINHAS = 100

interface Props {
  nome: string
  tabela: Tabela
  onFechar: () => void
}

function PreviaDoArquivo({ nome, tabela, onFechar }: Props) {
  const telaPequena = useMediaQuery(useTheme().breakpoints.down('sm'))
  const total = tabela.linhas.length
  const visiveis = tabela.linhas.slice(0, LIMITE_LINHAS)

  return (
    <Dialog open onClose={onFechar} maxWidth="lg" fullWidth fullScreen={telaPequena}>
      <DialogTitle sx={{ wordBreak: 'break-all' }}>{nome}</DialogTitle>
      <DialogContent dividers>
        <Typography color="text.secondary" sx={{ mb: 2 }}>
          {total > LIMITE_LINHAS
            ? `Mostrando os ${LIMITE_LINHAS} primeiros de ${total} registros.`
            : `${total} ${total === 1 ? 'registro' : 'registros'} no arquivo.`}
        </Typography>
        <TableContainer sx={{ maxHeight: { sm: 480 } }}>
          <Table stickyHeader size="small">
            <TableHead>
              <TableRow>
                <TableCell>#</TableCell>
                {tabela.colunas.map((coluna) => (
                  <TableCell key={coluna} sx={{ fontWeight: 700, whiteSpace: 'nowrap' }}>
                    {coluna}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {visiveis.map((linha, indice) => (
                <TableRow key={indice} hover>
                  <TableCell sx={{ color: 'text.secondary' }}>{indice + 1}</TableCell>
                  {tabela.colunas.map((coluna) => (
                    <TableCell key={coluna}>{linha[coluna] ?? ''}</TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </DialogContent>
      <DialogActions>
        <Button variant="contained" onClick={onFechar}>
          Fechar
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default PreviaDoArquivo
