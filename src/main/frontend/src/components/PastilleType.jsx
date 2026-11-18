import Icone, { nomIconeType } from './Icone.jsx'

// Rond à la couleur du type avec son pictogramme blanc (jamais la couleur seule).
export default function PastilleType({ type, taille = 32 }) {
  return (
    <span
      data-type={type}
      aria-hidden="true"
      style={{
        width: taille,
        height: taille,
        flex: 'none',
        borderRadius: '50%',
        background: 'var(--t)',
        color: '#fff',
        display: 'grid',
        placeItems: 'center',
      }}
    >
      <Icone nom={nomIconeType(type)} taille={16} />
    </span>
  )
}
