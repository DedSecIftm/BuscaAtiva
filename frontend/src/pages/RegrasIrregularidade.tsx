import { useEffect, useState } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControlLabel from '@mui/material/FormControlLabel'
import IconButton from '@mui/material/IconButton'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Switch from '@mui/material/Switch'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import AddIcon from '@mui/icons-material/Add'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import FormularioRegra from '../components/FormularioRegra.tsx'
import { carregarRegras, salvarRegras } from '../regras/armazenamento.ts'
import { descreverRegra } from '../regras/tipos.ts'
import type { DadosRegra, Regra } from '../regras/tipos.ts'

// null = formulário fechado; 'nova' = criando; Regra = editando.
type Edicao = null | 'nova' | Regra

function RegrasIrregularidade() {
  const [regras, setRegras] = useState<Regra[]>(carregarRegras)
  const [edicao, setEdicao] = useState<Edicao>(null)
  const [paraExcluir, setParaExcluir] = useState<Regra | null>(null)

  useEffect(() => salvarRegras(regras), [regras])

  function salvar(dados: DadosRegra) {
    if (edicao === 'nova') {
      setRegras((atuais) => [...atuais, { ...dados, id: crypto.randomUUID(), ativa: true }])
    } else if (edicao) {
      setRegras((atuais) => atuais.map((r) => (r.id === edicao.id ? { ...r, ...dados } : r)))
    }
    setEdicao(null)
  }

  function alternarAtiva(id: string) {
    setRegras((atuais) => atuais.map((r) => (r.id === id ? { ...r, ativa: !r.ativa } : r)))
  }

  function excluir() {
    setRegras((atuais) => atuais.filter((r) => r.id !== paraExcluir?.id))
    setParaExcluir(null)
  }

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        sx={{ justifyContent: 'space-between', alignItems: { sm: 'flex-end' } }}
      >
        <Box>
          <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 700 }}>
            Regras de irregularidade
          </Typography>
          <Typography color="text.secondary">
            Defina quando a frequência de um aluno deve ser considerada irregular.
          </Typography>
        </Box>
        <Button
          variant="contained"
          size="large"
          startIcon={<AddIcon />}
          onClick={() => setEdicao('nova')}
          sx={{ flexShrink: 0 }}
        >
          Nova regra
        </Button>
      </Stack>

      {regras.length === 0 ? (
        <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">
            Nenhuma regra cadastrada. Clique em "Nova regra" para criar a primeira.
          </Typography>
        </Paper>
      ) : (
        regras.map((regra) => (
          <Paper key={regra.id} variant="outlined" sx={{ p: 2, opacity: regra.ativa ? 1 : 0.6 }}>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={1}
              sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
            >
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
                <Typography variant="h6" component="h2">
                  {regra.nome}
                </Typography>
                <Chip
                  size="small"
                  label={regra.tipo === 'seguidas' ? 'Faltas seguidas' : 'Faltas no período'}
                  color={regra.tipo === 'seguidas' ? 'primary' : 'secondary'}
                  variant="outlined"
                />
              </Stack>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexShrink: 0 }}>
                <FormControlLabel
                  control={<Switch checked={regra.ativa} onChange={() => alternarAtiva(regra.id)} />}
                  label={regra.ativa ? 'Ativa' : 'Desativada'}
                  // Largura fixa para o switch não mudar de lugar quando o texto troca.
                  slotProps={{ typography: { sx: { width: '6rem' } } }}
                />
                <Tooltip title="Editar">
                  <IconButton aria-label={`Editar ${regra.nome}`} onClick={() => setEdicao(regra)}>
                    <EditOutlinedIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Excluir">
                  <IconButton aria-label={`Excluir ${regra.nome}`} onClick={() => setParaExcluir(regra)}>
                    <DeleteOutlinedIcon />
                  </IconButton>
                </Tooltip>
              </Stack>
            </Stack>
            <Typography color="text.secondary" sx={{ mt: 1, pt: 1, borderTop: 1, borderColor: 'divider' }}>
              Irregular quando: {descreverRegra(regra)}
            </Typography>
          </Paper>
        ))
      )}

      {edicao && (
        <FormularioRegra
          inicial={edicao === 'nova' ? undefined : edicao}
          onSalvar={salvar}
          onCancelar={() => setEdicao(null)}
        />
      )}

      <Dialog open={!!paraExcluir} onClose={() => setParaExcluir(null)}>
        <DialogTitle>Excluir regra?</DialogTitle>
        <DialogContent>
          <Typography>
            A regra <strong>{paraExcluir?.nome}</strong> será removida. Essa ação não pode ser desfeita.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setParaExcluir(null)}>Cancelar</Button>
          <Button color="error" variant="contained" onClick={excluir}>
            Excluir
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  )
}

export default RegrasIrregularidade
