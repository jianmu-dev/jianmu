import { useI18n } from 'vue-i18n';
import { globalT } from '@/locales';

export function useLocale() {
  const { locale, t } = useI18n();

  const handleLocaleChange = (lang: string) => {
    locale.value = lang;
    localStorage.setItem('JianmuLang', lang);
  };

  return {
    t,
    locale,
    handleLocaleChange,
  };
}
export { globalT };
