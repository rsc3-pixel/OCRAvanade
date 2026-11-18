// Ícones de traço desenhados num grid 24x24. Herdam a cor do texto (currentColor).
const caminhos = {
  upload: 'M7 18a4.5 4.5 0 0 1-.6-8.96A6 6 0 0 1 17.8 8.5 4 4 0 0 1 17 18M12 12v8m-3-5 3-3 3 3',
  arquivo: 'M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8zM14 3v5h5M9 13h6M9 17h4',
  check: 'm5 12.5 4.5 4.5L19 7.5',
  fechar: 'M6 6l12 12M18 6 6 18',
  alerta: 'M12 8v5m0 3.5v.01M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18z',
  seta: 'M5 12h14m-5-5 5 5-5 5',
  voltar: 'M19 12H5m5-5-5 5 5 5',
  agua: 'M12 3s6 6.5 6 11a6 6 0 0 1-12 0c0-4.5 6-11 6-11z',
  luz: 'M9 18h6m-5 3h4M12 3a6 6 0 0 0-3.5 10.9c.6.5 1 1.2 1 2V16h5v-.1c0-.8.4-1.5 1-2A6 6 0 0 0 12 3z',
  telefone: 'M5 4h3l2 5-2.5 1.5a11 11 0 0 0 6 6L15 14l5 2v3a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2z',
  cartao: 'M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM3 10h18M7 15h3',
  lista: 'M9 6h11M9 12h11M9 18h11M4.5 6h.01M4.5 12h.01M4.5 18h.01',
  enviar: 'M12 16V4m-4 4 4-4 4 4M5 14v4a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-4',
} as const

export type NomeIcone = keyof typeof caminhos

export function Icone({ nome, tamanho = 20 }: { nome: NomeIcone; tamanho?: number }) {
  return (
    <svg
      width={tamanho}
      height={tamanho}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={caminhos[nome]} />
    </svg>
  )
}
