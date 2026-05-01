import { Switch } from "@heroui/switch";
import { ThemeProps, useTheme } from "@heroui/use-theme";

import { FaMoonIcon, FaSunIcon } from "@/components/icons.tsx";

export const ThemeSwitch = () => {
  const { theme, setTheme } = useTheme(ThemeProps.DARK);

  return (
    <Switch
      color="primary"
      isSelected={theme === ThemeProps.LIGHT}
      size="lg"
      thumbIcon={({ isSelected, className }) =>
        isSelected ? (
          <FaSunIcon className={className} />
        ) : (
          <FaMoonIcon className={className} />
        )
      }
      onChange={(e) => {
        setTheme(e.currentTarget.checked ? ThemeProps.LIGHT : ThemeProps.DARK);
      }}
    />
  );
};
