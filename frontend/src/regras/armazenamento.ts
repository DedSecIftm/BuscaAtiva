import type { Regra } from './tipos.ts'

// Enquanto não há backend, as regras ficam salvas no navegador.
const CHAVE = 'buscaativa.regras'

const REGRAS_INICIAIS: Regra[] = [
  {
    id: 'exemplo-seguidas',
    nome: 'Faltas seguidas',
    tipo: 'seguidas',
    quantidadeFaltas: 3,
    periodoEmDias: 3,
    ativa: true,
  },
  {
    id: 'exemplo-periodo',
    nome: 'Faltas frequentes',
    tipo: 'noPeriodo',
    quantidadeFaltas: 3,
    periodoEmDias: 10,
    ativa: true,
  },
]

export function carregarRegras(): Regra[] {
  try {
    const salvas = localStorage.getItem(CHAVE)
    return salvas ? (JSON.parse(salvas) as Regra[]) : REGRAS_INICIAIS
  } catch {
    return REGRAS_INICIAIS
  }
}

export function salvarRegras(regras: Regra[]) {
  try {
    localStorage.setItem(CHAVE, JSON.stringify(regras))
  } catch {
    // Sem acesso ao armazenamento do navegador: as regras valem só até recarregar a página.
  }
}
