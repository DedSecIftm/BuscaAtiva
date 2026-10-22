import type { ReactNode } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import IconButton from '@mui/material/IconButton'
import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import CodeIcon from '@mui/icons-material/Code'
import DataObjectIcon from '@mui/icons-material/DataObject'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import ErrorOutlinedIcon from '@mui/icons-material/ErrorOutlined'
import TableChartIcon from '@mui/icons-material/TableChart'
import type { ArquivoCarregado } from '../importacao/tipos.ts'
import type { TipoArquivo } from '../importacao/lerArquivo.ts'

const ICONES: Record<TipoArquivo, ReactNode> = {
  Excel: <TableChartIcon color="success" />,
  XML: <CodeIcon color="warning" />,
  JSON: <DataObjectIcon color="info" />,
}

function formatarTamanho(bytes: number): string {
  if (bytes < 1024) return `${bytes} bytes`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function Situacao({ item }: { item: ArquivoCarregado }) {
  if (item.status === 'lendo') {
    return (
      <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', gap: 1 }}>
        <CircularProgress size={14} /> Lendo arquivo...
      </Box>
    )
  }
  if (item.status === 'erro') {
    return (
      <Typography component="span" color="error" variant="body2">
        {item.erro}
      </Typography>
    )
  }
  const total = item.tabela.linhas.length
  return <>{total === 1 ? '1 registro encontrado' : `${total} registros encontrados`}</>
}

interface Props {
  itens: ArquivoCarregado[]
  onRemover: (id: string) => void
  onVerDados: (item: ArquivoCarregado) => void
}

function ListaDeArquivos({ itens, onRemover, onVerDados }: Props) {
  return (
    <Paper variant="outlined">
      <Typography variant="h6" sx={{ px: 2, pt: 2 }}>
        Arquivos carregados
      </Typography>
      {itens.length === 0 ? (
        <Typography color="text.secondary" sx={{ p: 2 }}>
          Nenhum arquivo carregado ainda.
        </Typography>
      ) : (
        <List>
          {itens.map((item) => (
            <ListItem key={item.id} divider sx={{ flexWrap: 'wrap', gap: 1 }}>
              <ListItemIcon>
                {item.status === 'erro' ? <ErrorOutlinedIcon color="error" /> : ICONES[item.tipo]}
              </ListItemIcon>
              <ListItemText
                primary={item.arquivo.name}
                secondary={
                  <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                    <Chip label={item.tipo} size="small" component="span" />
                    {formatarTamanho(item.arquivo.size)} · <Situacao item={item} />
                  </Box>
                }
                slotProps={{ primary: { sx: { wordBreak: 'break-all' } }, secondary: { component: 'div' } }}
                sx={{ flex: '1 1 240px' }}
              />
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                {item.status === 'pronto' && (
                  <Button variant="outlined" size="small" onClick={() => onVerDados(item)}>
                    Ver dados
                  </Button>
                )}
                <Tooltip title="Remover">
                  <IconButton aria-label={`Remover ${item.arquivo.name}`} onClick={() => onRemover(item.id)}>
                    <DeleteOutlinedIcon />
                  </IconButton>
                </Tooltip>
              </Stack>
            </ListItem>
          ))}
        </List>
      )}
    </Paper>
  )
}

export default ListaDeArquivos
