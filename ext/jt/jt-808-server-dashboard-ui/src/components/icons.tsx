import * as React from "react";

import { IconSvgProps } from "@/types";

export const LogoIcon: React.FC<IconSvgProps> = ({
  size = 36,
  height,
  ...props
}) => (
  <svg
    fill="none"
    height={size || height}
    viewBox="0 0 37 32"
    width={size || height}
    {...props}
  >
    <path
      d="M5.53315 11.0907C5.79612 11.3489 6.0591 11.3489 6.32207 11.3489C7.111 11.3489 7.89993 10.5744 7.89993 9.80002V8.76746C10.2667 9.28374 12.6335 10.5744 13.6854 12.8977L13.9484 13.414L15.2632 10.8326L16.8411 7.99304C14.4743 5.66979 11.3186 4.12095 7.89993 3.60467V2.57211C7.89993 2.31397 7.89993 2.05583 7.63695 1.53955C7.111 1.02328 6.32207 0.765137 5.53315 1.28142L0.799582 4.89537C0.536607 4.89537 0.536607 5.15351 0.273631 5.41165C-0.25232 5.92792 0.010655 6.96048 0.799582 7.47676L5.53315 11.0907ZM36.0383 25.5465L31.3048 21.9326H31.0418C30.2529 21.6744 29.4639 21.9326 29.201 22.4488C28.938 22.707 28.938 22.9651 28.938 23.2233V24.2558C26.5712 23.7395 24.2044 22.4488 23.1525 20.1256L22.8895 19.6093L22.1006 21.1581L20.2598 24.7721C22.3636 27.3535 25.7823 29.1605 29.201 29.4186V30.7093C29.201 31.4837 29.9899 32 30.7788 32C31.0418 32 31.3048 32 31.5677 31.7419L36.3013 28.1279L36.5643 27.8698C37.0902 27.3535 37.0902 26.3209 36.8273 25.8047C36.5643 25.8047 36.3013 25.5465 36.0383 25.5465Z"
      fill="#D46545"
    />
    <path
      d="M5.57275 30.7446C5.83761 31 6.10247 31 6.36733 31C7.1619 31 7.95648 30.2339 7.95648 29.4678V28.4463C12.1942 27.9356 16.1671 25.6372 18.286 22.062L18.5508 21.5513L23.3183 12.358L23.5831 11.8472C24.6425 9.80426 26.7614 8.52741 29.1451 8.01666V9.03815C29.1451 9.80426 29.9397 10.315 30.7343 10.315C30.9991 10.315 31.264 10.315 31.5289 10.0596L36.2963 6.48444L36.5612 6.22907C37.0909 5.71833 37.0909 4.69685 36.826 4.18611C36.826 3.93074 36.5612 3.93074 36.2963 3.67537L31.5289 0.100183H31.264C30.4694 -0.155187 29.6749 0.100183 29.41 0.610923C29.1451 1.37703 29.1451 1.63241 29.1451 1.88778V2.90926C24.9074 3.42 20.9345 5.71833 18.8157 9.29352L18.5508 10.0596L13.7834 19.253L13.5185 19.7637C12.4591 21.5513 10.3402 22.8281 7.95648 23.3389V22.5728C7.95648 22.3174 7.95648 22.062 7.69162 21.5513C7.1619 20.7852 6.36733 20.5298 5.57275 21.0406L0.805306 24.6157C0.540448 24.6157 0.540448 24.8711 0.27559 25.1265C-0.254126 25.8926 0.0107313 26.9141 0.805306 27.4248L5.57275 30.7446Z"
      fill="#3070FF"
    />
  </svg>
);

export const GithubIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="0 0 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M12.026 2c-5.509 0-9.974 4.465-9.974 9.974 0 4.406 2.857 8.145 6.821 9.465.499.09.679-.217.679-.481 0-.237-.008-.865-.011-1.696-2.775.602-3.361-1.338-3.361-1.338-.452-1.152-1.107-1.459-1.107-1.459-.905-.619.069-.605.069-.605 1.002.07 1.527 1.028 1.527 1.028.89 1.524 2.336 1.084 2.902.829.091-.645.351-1.085.635-1.334-2.214-.251-4.542-1.107-4.542-4.93 0-1.087.389-1.979 1.024-2.675-.101-.253-.446-1.268.099-2.64 0 0 .837-.269 2.742 1.021a9.582 9.582 0 0 1 2.496-.336 9.554 9.554 0 0 1 2.496.336c1.906-1.291 2.742-1.021 2.742-1.021.545 1.372.203 2.387.099 2.64.64.696 1.024 1.587 1.024 2.675 0 3.833-2.33 4.675-4.552 4.922.355.308.675.916.675 1.846 0 1.334-.012 2.41-.012 2.737 0 .267.178.577.687.479C19.146 20.115 22 16.379 22 11.974 22 6.465 17.535 2 12.026 2z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};

export const MoonFilledIcon = ({
  size = 24,
  width,
  height,
  ...props
}: IconSvgProps) => (
  <svg
    aria-hidden="true"
    focusable="false"
    height={size || height}
    role="presentation"
    viewBox="0 0 24 24"
    width={size || width}
    {...props}
  >
    <path
      d="M21.53 15.93c-.16-.27-.61-.69-1.73-.49a8.46 8.46 0 01-1.88.13 8.409 8.409 0 01-5.91-2.82 8.068 8.068 0 01-1.44-8.66c.44-1.01.13-1.54-.09-1.76s-.77-.55-1.83-.11a10.318 10.318 0 00-6.32 10.21 10.475 10.475 0 007.04 8.99 10 10 0 002.89.55c.16.01.32.02.48.02a10.5 10.5 0 008.47-4.27c.67-.93.49-1.519.32-1.79z"
      fill="currentColor"
    />
  </svg>
);

export const SunFilledIcon = ({
  size = 24,
  width,
  height,
  ...props
}: IconSvgProps) => (
  <svg
    aria-hidden="true"
    focusable="false"
    height={size || height}
    role="presentation"
    viewBox="0 0 24 24"
    width={size || width}
    {...props}
  >
    <g fill="currentColor">
      <path d="M19 12a7 7 0 11-7-7 7 7 0 017 7z" />
      <path d="M12 22.96a.969.969 0 01-1-.96v-.08a1 1 0 012 0 1.038 1.038 0 01-1 1.04zm7.14-2.82a1.024 1.024 0 01-.71-.29l-.13-.13a1 1 0 011.41-1.41l.13.13a1 1 0 010 1.41.984.984 0 01-.7.29zm-14.28 0a1.024 1.024 0 01-.71-.29 1 1 0 010-1.41l.13-.13a1 1 0 011.41 1.41l-.13.13a1 1 0 01-.7.29zM22 13h-.08a1 1 0 010-2 1.038 1.038 0 011.04 1 .969.969 0 01-.96 1zM2.08 13H2a1 1 0 010-2 1.038 1.038 0 011.04 1 .969.969 0 01-.96 1zm16.93-7.01a1.024 1.024 0 01-.71-.29 1 1 0 010-1.41l.13-.13a1 1 0 011.41 1.41l-.13.13a.984.984 0 01-.7.29zm-14.02 0a1.024 1.024 0 01-.71-.29l-.13-.14a1 1 0 011.41-1.41l.13.13a1 1 0 010 1.41.97.97 0 01-.7.3zM12 3.04a.969.969 0 01-1-.96V2a1 1 0 012 0 1.038 1.038 0 01-1 1.04z" />
    </g>
  </svg>
);

export const HeartFilledIcon = ({
  size = 24,
  width,
  height,
  ...props
}: IconSvgProps) => (
  <svg
    aria-hidden="true"
    focusable="false"
    height={size || height}
    role="presentation"
    viewBox="0 0 24 24"
    width={size || width}
    {...props}
  >
    <path
      d="M12.62 20.81c-.34.12-.9.12-1.24 0C8.48 19.82 2 15.69 2 8.69 2 5.6 4.49 3.1 7.56 3.1c1.82 0 3.43.88 4.44 2.24a5.53 5.53 0 0 1 4.44-2.24C19.51 3.1 22 5.6 22 8.69c0 7-6.48 11.13-9.38 12.12Z"
      fill="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={1.5}
    />
  </svg>
);

export const SearchIcon = (props: IconSvgProps) => (
  <svg
    aria-hidden="true"
    fill="none"
    focusable="false"
    height="1em"
    role="presentation"
    viewBox="0 0 24 24"
    width="1em"
    {...props}
  >
    <path
      d="M11.5 21C16.7467 21 21 16.7467 21 11.5C21 6.25329 16.7467 2 11.5 2C6.25329 2 2 6.25329 2 11.5C2 16.7467 6.25329 21 11.5 21Z"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="2"
    />
    <path
      d="M22 22L20 20"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="2"
    />
  </svg>
);

export const EyeIcon = (props: IconSvgProps) => (
  <svg
    aria-hidden="true"
    fill="none"
    focusable="false"
    height="1em"
    role="presentation"
    viewBox="0 0 24 24"
    width="1em"
    {...props}
  >
    <path
      d="M12.9833 10C12.9833 11.65 11.65 12.9833 10 12.9833C8.35 12.9833 7.01666 11.65 7.01666 10C7.01666 8.35 8.35 7.01666 10 7.01666C11.65 7.01666 12.9833 8.35 12.9833 10Z"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={1.5}
    />
    <path
      d="M9.99999 16.8916C12.9417 16.8916 15.6833 15.1583 17.5917 12.1583C18.3417 10.9833 18.3417 9.00831 17.5917 7.83331C15.6833 4.83331 12.9417 3.09998 9.99999 3.09998C7.05833 3.09998 4.31666 4.83331 2.40833 7.83331C1.65833 9.00831 1.65833 10.9833 2.40833 12.1583C4.31666 15.1583 7.05833 16.8916 9.99999 16.8916Z"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={1.5}
    />
  </svg>
);

export const DelIcon = (props: IconSvgProps) => (
  <svg
    aria-hidden="true"
    fill="none"
    focusable="false"
    height="1em"
    role="presentation"
    viewBox="0 0 24 24"
    width="1em"
    {...props}
  >
    <path
      d="M17.5 4.98332C14.725 4.70832 11.9333 4.56665 9.15 4.56665C7.5 4.56665 5.85 4.64998 4.2 4.81665L2.5 4.98332"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={1.5}
    />
    <path
      d="M7.08331 4.14169L7.26665 3.05002C7.39998 2.25835 7.49998 1.66669 8.90831 1.66669H11.0916C12.5 1.66669 12.6083 2.29169 12.7333 3.05835L12.9166 4.14169"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={1.5}
    />
    <path
      d="M15.7084 7.61664L15.1667 16.0083C15.075 17.3166 15 18.3333 12.675 18.3333H7.32502C5.00002 18.3333 4.92502 17.3166 4.83335 16.0083L4.29169 7.61664"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={1.5}
    />
    <path
      d="M8.60834 13.75H11.3833"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={1.5}
    />
    <path
      d="M7.91669 10.4167H12.0834"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={1.5}
    />
  </svg>
);

export const ChevronDownIcon = ({
  strokeWidth = 1.5,
  ...otherProps
}: IconSvgProps) => (
  <svg
    aria-hidden="true"
    fill="none"
    focusable="false"
    height="1em"
    role="presentation"
    viewBox="0 0 24 24"
    width="1em"
    {...otherProps}
  >
    <path
      d="m19.92 8.95-6.52 6.52c-.77.77-2.03.77-2.8 0L4.08 8.95"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeMiterlimit={10}
      strokeWidth={strokeWidth}
    />
  </svg>
);

export const DashboardIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="-2 -2 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M19.6335 7.3637c-.5143-1.2151-1.2493-2.3051-2.1845-3.2404-.9353-.9353-2.0252-1.6703-3.2404-2.1845C12.9502 1.4063 11.6167 1.1378 10.24 1.1378s-2.7102.2685-3.9686.801c-1.2151.5143-2.3051 1.2493-3.2404 2.1845-.9353.9353-1.6703 2.0252-2.1845 3.2404C.314 8.6221.0455 9.9556.0455 11.3323c0 3.0197 1.3266 5.8641 3.6386 7.8074l.0387.0319c.132.1092.2981.1707.4688.1707h12.0991c.1707 0 .3368-.0614.4688-.1707l.0387-.0319C19.1078 17.1964 20.4345 14.3519 20.4345 11.3323c0-1.3767-.2708-2.7102-.801-3.9686zM9.5573 3.8684c0-.1001.0819-.182.182-.182h1.0012c.1001 0 .182.0819.182.182v1.8204c0 .1001-.0819.182-.182.182h-1.0012c-.1001 0-.182-.0819-.182-.182v-1.8204zM4.7332 11.8329c0 .1001-.0819.182-.182.182h-1.8204c-.1001 0-.182-.0819-.182-.182v-1.0012c0-.1001.0819-.182.182-.182h1.8204c.1001 0 .182.0819.182.182v1.0012zm2.0639-4.6535-.7077.7077c-.0705.0705-.1866.0705-.2571 0L4.5443 6.5991c-.0705-.0705-.0705-.1866 0-.2571l.7077-.7077c.0705-.0705.1866-.0705.2571 0l1.288 1.288c.0705.0705.0705.1866 0 .2571zm6.6241 1.9024-1.9228 1.9228c.1138.4255.0046.8966-.33 1.2311-.4983.4983-1.3039.4983-1.8022 0-.4983-.4983-.4983-1.3039 0-1.8022.3345-.3345.8055-.4437 1.2311-.33l1.9228-1.9228c.0705-.0705.1866-.0705.2571 0l.644.644c.0705.0705.0705.1843 0 .2571zm.9785-1.1924-.7077-.7077c-.0705-.0705-.0705-.1866 0-.2571l1.288-1.288c.0705-.0705.1866-.0705.2571-0l.7077.7077c.0705.0705.0705.1866 0 .2571l-1.288 1.288c-.0705.0705-.1866.0705-.2571 0zM17.8404 11.8329c0 .1001-.0819.182-.182.182h-1.8204c-.1001 0-.182-.0819-.182-.182v-1.0012c0-.1001.0819-.182.182-.182h1.8204c.1001 0 .182.0819.182.182v1.0012z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};

export const SessionIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="-2 -2 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M16.7005 11.3715c0 1.0013-.7702 1.8202-1.7116 1.8202h-12.6963c-.9413 0-1.7115-.8188-1.7115-1.8202V2.4453c0-1.0009.7702-1.8198 1.7114-1.8198h12.6963c.9415 0 1.7116.8188 1.7116 1.8198V11.3715z"
        fill="currentColor"
        fillRule="evenodd"
      />
      <path
        clipRule="evenodd"
        d="M4.165 16.7149c-.6506.6662-.7542.1044-.7542-.5374l0-3.7072c.2413-.9614 2.0648-1.5217 2.9027-1.2449l.9042.299c.8378.2773.9909 1.0492.3404 1.7154L4.165 16.7149zM17.6879 3.2369c-.3435-.3652-.3573.3092-.3573.3092V11.8713c0 1.0625-.8173 1.9316-1.8164 1.9316H8.6229s-.3362-.0125-.5472.2113c-.2287.2898-1.2598 1.3947-1.5236 1.7635-.2637.3695.2368.334.2368.334h11.3999c.9413 0 1.7114-.819 1.7114-1.8196V6.1057c0-.1686-.0309-.5293-.2215-.7318-.1903-.2026-1.5974-1.7183-1.991-2.1369z"
        fill="currentColor"
        fillRule="evenodd"
      />
      <path
        clipRule="evenodd"
        d="M15.9462 19.6448c.6506.6663.7543.1044.7543-.5368V15.4009c-.2415-.9621-2.065-1.5221-2.9027-1.2449l-.9041.2985c-.8379.2773-.991 1.0492-.3405 1.7154l3.3931 3.475z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};
export const AttachmentIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="-2 -2 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M3.2892 19.1767A2.9789 2.9789 90 01.3103 16.1978V3.2892a1.9859 1.9859 90 011.9859-1.9859h5.8883a1.9859 1.9859 90 011.6518.8842l1.3966 2.0947h5.9578a2.9789 2.9789 90 012.9789 2.9789v8.9367a2.9789 2.9789 90 01-2.9789 2.9789H3.2892z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};

export const CommandIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="-2 -2 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M18.614 1.1378H1.866c-.4028 0-.7282.3254-.7282.7282v16.7481c0 .4028.3254.7282.7282.7282h16.7481c.4028 0 .7282-.3254.7282-.7282V1.866c0-.4028-.3254-.7282-.7282-.7282zM10.265 10.3788l-4.3691 3.6636c-.1183.1001-.2981.0159-.2981-.1388v-1.4268c0-.0523.025-.1047.066-.1388L8.1624 10.24l-2.4986-2.0981c-.0432-.0341-.066-.0842-.066-.1388V6.5764c0-.1547.1798-.2389.2981-.1388l4.3691 3.6614c.0887.0728.0887.2071 0 .2799zM14.8821 13.9036c0 .1001-.0774.182-.1707.182h-4.2098c-.0933 0-.1707-.0819-.1707-.182v-1.0923c0-.1001.0774-.182.1707-.182h4.2098c.0933 0 .1707.0819.1707.182v1.0923z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};

// export const ControlIcon: React.FC<IconSvgProps> = ({
//   size = 24,
//   width,
//   height,
//   ...props
// }) => {
//   return (
//     <svg
//       height={size || height}
//       viewBox="-2 -2 24 24"
//       width={size || width}
//       {...props}
//     >
//       <path
//         clipRule="evenodd"
//         d="M18.614 1.1378H1.866c-.4028 0-.7282.3254-.7282.7282v16.7481c0 .4028.3254.7282.7282.7282h16.7481c.4028 0 .7282-.3254.7282-.7282V1.866c0-.4028-.3254-.7282-.7282-.7282zM7.7824 14.1312v1.7522c0 .1001-.0819.182-.182.182h-1.0923c-.1001 0-.182-.0819-.182-.182v-1.7522c-.9489-.3095-1.6384-1.2015-1.6384-2.2528s.6895-1.9456 1.6384-2.2528V4.5966c0-.1001.0819-.182.182-.182h1.0923c.1001 0 .182.0819.182.182v5.029c.9489.3095 1.6384 1.2015 1.6384 2.2528s-.6895 1.9456-1.6384 2.2528zm6.3625-3.2745c.0046 0 .0068-.0023.0091-.0023v5.029c0 .1001-.0819.182-.182.182h-1.0923c-.1001 0-.182-.0819-.182-.182V10.8544c.0046 0 .0068.0023.0091.0023-.9557-.3049-1.6475-1.1992-1.6475-2.2551 0-1.0559.6918-1.9502 1.6475-2.2551-.0046 0-.0068.0023-.0091.0023v-1.7522c0-.1001.0819-.182.182-.182h1.0923c.1001 0 .182.0819.182.182v1.7522c-.0046 0-.0068-.0023-.0091-.0023.9557.3049 1.6475 1.1992 1.6475 2.2551 0 1.0559-.6918 1.9502-1.6475 2.2551z"
//         fill="currentColor"
//         fillRule="evenodd"
//       />
//       <path
//         clipRule="evenodd"
//         d="M13.4258 8.6016m-.8192 0a.8192.8192 90 101.6384 0 .8192.8192 90 10-1.6384 0Z"
//         fill="currentColor"
//         fillRule="evenodd"
//       />
//       <path
//         clipRule="evenodd"
//         d="M7.7687 11.4802l-.0341-.0546c0-.0023-.0023-.0023-.0023-.0046l-.0205-.0273c-.0023-.0023-.0046-.0046-.0046-.0068-.0228-.0296-.0455-.0569-.0728-.0819l-.0046-.0046c-.0091-.0091-.0182-.0182-.0273-.025-.0182-.0182-.0387-.0341-.0592-.0478h-.0023l-.0273-.0205c-.0023-.0023-.0068-.0046-.0091-.0068-.0273-.0182-.0569-.0364-.0887-.0501-.0046-.0023-.0114-.0046-.0159-.0091-.0091-.0046-.0159-.0068-.025-.0114-.0068-.0023-.0159-.0068-.0228-.0091-.0114-.0046-.0228-.0091-.0341-.0114-.0091-.0023-.0205-.0068-.0296-.0091l-.0205-.0068c-.0114-.0023-.0205-.0046-.0319-.0068-.0046-.0023-.0114-.0023-.0159-.0046-.0159-.0023-.0319-.0068-.0478-.0091-.0046 0-.0091 0-.0137-.0023-.0137-.0023-.025-.0023-.0387-.0046-.0046 0-.0091 0-.0159-.0023-.0182 0-.0341-.0023-.0523-.0023s-.0341 0-.0523.0023c-.0046 0-.0091 0-.0159.0023-.0137 0-.0273.0023-.0387.0046-.0046 0-.0091 0-.0137.0023-.0159.0023-.0319.0046-.0478.0091-.0046.0023-.0114.0023-.0159.0046-.0114.0023-.0205.0046-.0319.0068l-.0205.0068c-.0091.0023-.0205.0068-.0296.0091-.0114.0046-.0228.0091-.0341.0114-.0068.0023-.0159.0068-.0228.0091-.0091.0046-.0159.0068-.025.0114-.0046.0023-.0114.0046-.0159.0091-.0296.0159-.0592.0319-.0887.0501-.0023.0023-.0068.0046-.0091.0068l-.0273.0205h-.0023c-.0205.0159-.041.0319-.0592.0478-.0091.0091-.0182.0159-.0273.025l-.0046.0046c-.025.025-.0501.0546-.0728.0819-.0023.0023-.0046.0046-.0046.0068l-.0205.0273c0 .0023-.0023.0023-.0023.0046l-.0341.0546c-.0023.0046-.0046.0068-.0068.0114-.0614.1161-.0978.248-.0978.3868s.0364.2731.0978.3868c.0023.0046.0046.0068.0068.0114l.0341.0546c0 .0023.0023.0023.0023.0046l.0205.0273c.0023.0023.0046.0046.0046.0068.0228.0296.0455.0569.0728.0819l.0046.0046c.0091.0091.0182.0182.0273.025.0182.0182.0387.0341.0592.0478h.0023l.0273.0205c.0023.0023.0068.0046.0091.0068.0273.0182.0569.0364.0887.0501.0046.0023.0114.0046.0159.0091.0091.0046.0159.0068.025.0114.0068.0023.0159.0068.0228.0091.0114.0046.0228.0091.0341.0114.0091.0023.0205.0068.0296.0091l.0205.0068c.0114.0023.0205.0046.0319.0068.0046.0023.0114.0023.0159.0046.0159.0023.0319.0068.0478.0091.0046 0 .0091 0 .0137.0023.0137.0023.025.0023.0387.0046.0046 0 .0091 0 .0159.0023.0182 0 .0341.0023.0523.0023s.0341 0 .0523-.0023c.0046 0 .0091 0 .0159-.0023.0137 0 .0273-.0023.0387-.0046.0046 0 .0091 0 .0137-.0023.0159-.0023.0319-.0046.0478-.0091.0046-.0023.0114-.0023.0159-.0046.0114-.0023.0205-.0046.0319-.0068l.0205-.0068c.0091-.0023.0205-.0068.0296-.0091.0114-.0046.0228-.0091.0341-.0114.0068-.0023.0159-.0068.0228-.0091.0091-.0046.0159-.0068.025-.0114.0046-.0023.0114-.0046.0159-.0091.0296-.0159.0592-.0319.0887-.0501.0023-.0023.0068-.0046.0091-.0068l.0273-.0205h.0023c.0205-.0159.041-.0319.0592-.0478.0091-.0091.0182-.0159.0273-.025l.0046-.0046c.025-.025.0501-.0546.0728-.0819.0023-.0023.0046-.0046.0046-.0068l.0205-.0273c0-.0023.0023-.0023.0023-.0046l.0341-.0546c.0023-.0046.0046-.0068.0068-.0114.0614-.1161.0978-.248.0978-.3868s-.0364-.2731-.0978-.3868c-.0023-.0046-.0046-.0091-.0068-.0114z"
//         fill="currentColor"
//         fillRule="evenodd"
//       />
//     </svg>
//   );
// };
export const SubIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="-2 -2 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M18.7965 3.2278c0-1.0852-.6678-2.0035-1.5861-2.4209-.2922-.0835-.6261-.167-.96-.167H4.313C2.8522.64 1.6835 1.8087 1.6835 3.2278v14.9009C1.6835 19.4226 3.1443 20.2574 4.2713 19.5478l5.7183-3.4226c.1252-.0835.2504-.0835.3339 0l5.927 3.4643C17.3774 20.2574 18.7965 19.4226 18.7965 18.1287V3.2278c0 .0417 0 .0417 0 0zM7.2765 9.7809c-.5843 0-1.0435-.4591-1.0435-1.0435s.4591-1.0435 1.0435-1.0435H9.1965v-1.92c0-.5843.4591-1.0435 1.0435-1.0435s1.0435.4591 1.0435 1.0435v1.92h1.92c.5843 0 1.0435.4591 1.0435 1.0435s-.4591 1.0435-1.0435 1.0435H11.2835v1.92c0 .5843-.4591 1.0435-1.0435 1.0435S9.1965 12.2852 9.1965 11.7009v-1.92h-1.92z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};

export const ConfigIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="-2 -2 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M10.2507 7.6501c-.6379 0-1.2352.2475-1.6875.6997-.4501.4523-.6997 1.0496-.6997 1.6875 0 .6379.2496 1.2352.6997 1.6875.4523.4501 1.0496.6997 1.6875.6997.6379 0 1.2352-.2496 1.6875-.6997.4501-.4523.6997-1.0496.6997-1.6875 0-.6379-.2496-1.2352-.6997-1.6875-.4523-.4523-1.0496-.6997-1.6875-.6997z"
        fill="currentColor"
        fillRule="evenodd"
      />
      <path
        clipRule="evenodd"
        d="M19.0464 12.6741l-1.3952-1.1925c.0661-.4053.1003-.8192.1003-1.2309s-.0341-.8277-.1003-1.2309l1.3952-1.1925c.2155-.1835.2944-.4821.1984-.7509l-.0192-.0555c-.3861-1.0752-.9557-2.0651-1.6981-2.9376l-.0384-.0448c-.1835-.2155-.48-.2965-.7488-.2027l-1.7323.6165c-.64-.5248-1.3525-.9387-2.1248-1.2267l-.3349-1.8112c-.0512-.2795-.2709-.4971-.5504-.5483l-.0576-.0107c-1.1093-.2005-2.2784-.2005-3.3877 0l-.0576.0107c-.2795.0512-.4992.2688-.5504.5483l-.3371 1.8197c-.7659.2901-1.4741.7019-2.1099 1.2224l-1.7451-.6208c-.2667-.0939-.5653-.0149-.7488.2027l-.0384.0448c-.7424.8768-1.312 1.8645-1.6981 2.9376l-.0192.0555c-.096.2667-.0171.5653.1984.7509l1.4123 1.2053c-.0661.4011-.0981.8107-.0981 1.216 0 .4096.032.8192.0981 1.216l-1.408 1.2053c-.2155.1835-.2944.4821-.1984.7509l.0192.0555c.3861 1.0731.9557 2.0651 1.6981 2.9376l.0384.0448c.1835.2155.48.2965.7488.2027l1.7451-.6208c.6357.5227 1.344.9365 2.1099 1.2224l.3371 1.8197c.0512.2795.2709.4971.5504.5483l.0576.0107c.5568.1003 1.1243.1515 1.6939.1515.5696 0 1.1392-.0512 1.6939-.1515l.0576-.0107c.2795-.0512.4992-.2688.5504-.5483l.3349-1.8112c.7723-.2901 1.4848-.7019 2.1248-1.2267l1.7323.6165c.2667.0939.5653.0149.7488-.2027l.0384-.0448c.7424-.8768 1.312-1.8645 1.6981-2.9376l.0192-.0555c.0917-.2645.0128-.5611-.2027-.7467zm-8.7957 1.1136c-2.0715 0-3.7504-1.6789-3.7504-3.7504s1.6789-3.7504 3.7504-3.7504 3.7504 1.6789 3.7504 3.7504-1.6789 3.7504-3.7504 3.7504z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};