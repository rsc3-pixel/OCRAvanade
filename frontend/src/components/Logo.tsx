/**
 * Marca "Atesta": documento + linha de leitura (o OCR passando) + check (o veredito).
 * As cores vêm dos tokens do CSS (--marca, --destaque), então o logo acompanha o tema escuro.
 */
export function Logo({ compacto = false }: { compacto?: boolean }) {
  return (
    <span className="logo">
      <svg className="logo__simbolo" viewBox="0 0 48 48" aria-hidden="true">
        <rect width="48" height="48" rx="12" fill="var(--marca)" />
        <path d="M15 10h13l7 7v21a2 2 0 0 1-2 2H15a2 2 0 0 1-2-2V12a2 2 0 0 1 2-2z" fill="var(--logo-papel)" />
        <path d="M28 10v7h7" fill="var(--logo-dobra)" />
        <rect className="logo__scanner" x="11" y="22" width="26" height="3" rx="1.5" fill="var(--destaque)" />
        <path d="m18.5 31 3.5 3.5 7-7" fill="none" stroke="var(--marca)" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
      {!compacto && <span className="logo__nome">atesta</span>}
    </span>
  )
}
