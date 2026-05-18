import { Switch, useTheme } from "@heroui/react";

import { LuMoonIcon, LuSunIcon } from "@/components/icons.tsx";

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
      className="scale-105"
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
              <LuSunIcon className="text-sm" />
            ) : (
              <LuMoonIcon className="text-sm" />
            )}
          </Switch.Thumb>
        </Switch.Control>
      </Switch.Content>
    </Switch>
  );
};
