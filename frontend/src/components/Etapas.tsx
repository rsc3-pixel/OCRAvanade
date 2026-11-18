import { Icone } from './Icone'

const ETAPAS = [
  { titulo: 'Upload', legenda: 'Envio do documento' },
  { titulo: 'Processamento', legenda: 'OpenCV e OCR' },
  { titulo: 'Validação', legenda: 'Regras de negócio' },
  { titulo: 'Resultado', legenda: 'Aprovado ou reprovado' },
]

/** atual = índice da etapa em andamento. Etapas antes dela aparecem como concluídas. */
export function Etapas({ atual }: { atual: number }) {
  return (
    <ol className="etapas" aria-label="Progresso do envio">
      {ETAPAS.map((etapa, i) => {
        const estado = i < atual ? 'feita' : i === atual ? 'atual' : 'futura'
        return (
          <li key={etapa.titulo} className={`etapa etapa--${estado}`} aria-current={estado === 'atual' ? 'step' : undefined}>
            <span className="etapa__numero">{estado === 'feita' ? <Icone nome="check" tamanho={16} /> : i + 1}</span>
            <span className="etapa__texto">
              <strong>{etapa.titulo}</strong>
              <small>{etapa.legenda}</small>
            </span>
          </li>
        )
      })}
    </ol>
  )
}
