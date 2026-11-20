import { useRef, useState } from 'react'
import type { ChangeEvent, DragEvent } from 'react'
import Button from '@mui/material/Button'
import Paper from '@mui/material/Paper'
import Typography from '@mui/material/Typography'
import CloudUploadIcon from '@mui/icons-material/CloudUpload'

interface Props {
  onArquivos: (arquivos: FileList) => void
}

function AreaDeEnvio({ onArquivos }: Props) {
  const [arrastando, setArrastando] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  function aoSelecionar(evento: ChangeEvent<HTMLInputElement>) {
    if (evento.target.files) onArquivos(evento.target.files)
    evento.target.value = ''
  }

  function aoSoltar(evento: DragEvent) {
    evento.preventDefault()
    setArrastando(false)
    onArquivos(evento.dataTransfer.files)
  }

  return (
    <Paper
      variant="outlined"
      onDragOver={(e) => {
        e.preventDefault()
        setArrastando(true)
      }}
      onDragLeave={() => setArrastando(false)}
      onDrop={aoSoltar}
      sx={{
        p: { xs: 3, sm: 5 },
        textAlign: 'center',
        borderStyle: 'dashed',
        borderWidth: 2,
        borderColor: arrastando ? 'primary.main' : 'divider',
        bgcolor: arrastando ? 'action.hover' : 'background.paper',
        transition: 'all 0.2s',
      }}
    >
      <CloudUploadIcon color="primary" sx={{ fontSize: 64 }} />
      <Typography variant="h6" sx={{ mt: 1 }}>
        Arraste os arquivos aqui
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        ou
      </Typography>
      <Button variant="contained" size="large" onClick={() => inputRef.current?.click()}>
        Escolher arquivos
      </Button>
      <input
        ref={inputRef}
        type="file"
        hidden
        multiple
        accept=".xlsx"
        onChange={aoSelecionar}
      />
    </Paper>
  )
}

export default AreaDeEnvio
