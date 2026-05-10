import { Switch, useTheme } from "@heroui/react";

import { FaMoonIcon, FaSunIcon } from "@/components/icons.tsx";

function resolveUiTheme(theme: string) {
  if (theme === "system") {
    return window.matchMedia("(prefers-color-scheme: dark)").matches
      ? "dark"
      : "light";
  }

  return theme === "dark" ? "dark" : "light";
}

export const ThemeSwitch = () => {
  const { theme, setTheme } = useTheme("dark");
  const resolved = resolveUiTheme(theme);

  return (
    <Switch
      className="scale-110"
      isSelected={resolved === "light"}
      size="lg"
      onChange={(isSelected) => {
        setTheme(isSelected ? "light" : "dark");
      }}
    >
      <Switch.Content>
        <Switch.Control>
          <Switch.Thumb>
            {resolved === "light" ? (
              <FaSunIcon className="text-sm" />
            ) : (
              <FaMoonIcon className="text-sm" />
            )}
          </Switch.Thumb>
        </Switch.Control>
      </Switch.Content>
    </Switch>
  );
};
