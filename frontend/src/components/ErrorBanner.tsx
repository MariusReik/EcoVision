import { ApiError } from '../api/client'

export function ErrorBanner({ error }: { error: unknown }) {
  if (!error) {
    return null
  }

  const message = error instanceof ApiError ? error.message : 'Something went wrong. Please try again.'

  return (
    <div role="alert" className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
      {message}
    </div>
  )
}
