import { useCallback, useEffect, useState } from 'react'

export const navigate = (to: string, replace = false) => {
  if (replace) window.history.replaceState({}, '', to)
  else window.history.pushState({}, '', to)
  window.dispatchEvent(new PopStateEvent('popstate'))
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

export const usePathname = () => {
  const [pathname, setPathname] = useState(window.location.pathname)
  useEffect(() => {
    const onChange = () => setPathname(window.location.pathname)
    window.addEventListener('popstate', onChange)
    return () => window.removeEventListener('popstate', onChange)
  }, [])
  return pathname
}

export const useNavigate = () => useCallback((to: string, replace = false) => navigate(to, replace), [])

