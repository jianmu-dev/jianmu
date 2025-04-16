import { useI18n } from 'vue-i18n';

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
