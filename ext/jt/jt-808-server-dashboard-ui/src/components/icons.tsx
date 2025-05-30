import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub } from "@fortawesome/free-brands-svg-icons";
import {
  faBug,
  faChartSimple,
  faChevronDown,
  faChevronRight,
  faCircleCheck,
  faClone,
  faCube,
  faDesktop,
  faEye,
  faFileCirclePlus,
  faGear,
  faHashtag,
  faHeart,
  faMoon,
  faQuoteRight,
  faRobot,
  faServer,
  faShuffle,
  faSun,
  faTags,
  faTrash,
  IconDefinition,
} from "@fortawesome/free-solid-svg-icons";
import {
  faGauge,
  faComments,
  faTerminal,
} from "@fortawesome/free-solid-svg-icons";
import * as React from "react";

import { IconSvgProps } from "@/types";
const FaIcon = ({ icon, ...props }: { icon: IconDefinition }) => {
  return (
    <FontAwesomeIcon
      className="text-default-500 text-xl"
      icon={icon}
      {...props}
    />
  );
};

export const FaGithubIcon = ({ ...props }) => {
  return <FaIcon icon={faGithub} {...props} />;
};
export const FaSunIcon = ({ ...props }) => {
  return <FaIcon icon={faSun} {...props} />;
};
export const FaMoonIcon = ({ ...props }) => {
  return <FaIcon icon={faMoon} {...props} />;
};
export const FaGaugeIcon = ({ ...props }) => {
  return <FaIcon icon={faGauge} {...props} />;
};
export const FaBugIcon = ({ ...props }) => {
  return <FaIcon icon={faBug} {...props} />;
};
export const FaCommentsIcon = ({ ...props }) => {
  return <FaIcon icon={faComments} {...props} />;
};
export const FaTerminalIcon = ({ ...props }) => {
  return <FaIcon icon={faTerminal} {...props} />;
};
export const FaHeatIcon = ({ ...props }) => {
  return <FaIcon icon={faHeart} {...props} />;
};
export const FaTagsIcon = ({ ...props }) => {
  return <FaIcon icon={faTags} {...props} />;
};
export const FaEyeIcon = ({ ...props }) => {
  return <FaIcon icon={faEye} {...props} />;
};
export const FaTrashIcon = ({ ...props }) => {
  return <FaIcon icon={faTrash} {...props} />;
};
export const FaChevronDownIcon = ({ ...props }) => {
  return <FaIcon icon={faChevronDown} {...props} />;
};
export const FaFileCirclePlusIcon = ({ ...props }) => {
  return <FaIcon icon={faFileCirclePlus} {...props} />;
};
export const FaGearIcon = ({ ...props }) => {
  return <FaIcon icon={faGear} {...props} />;
};
export const FaShuffleIcon = ({ ...props }) => {
  return <FaIcon icon={faShuffle} {...props} />;
};
export const FaCloneIcon = ({ ...props }) => {
  return <FaIcon icon={faClone} {...props} />;
};
export const FaChartSimpleIcon = ({ ...props }) => {
  return <FaIcon icon={faChartSimple} {...props} />;
};
export const FaServerIcon = ({ ...props }) => {
  return <FaIcon icon={faServer} {...props} />;
};
export const FaDesktopIcon = ({ ...props }) => {
  return <FaIcon icon={faDesktop} {...props} />;
};
export const FaRobotIcon = ({ ...props }) => {
  return <FaIcon icon={faRobot} {...props} />;
};
export const FaHashtagIcon = ({ ...props }) => {
  return <FaIcon icon={faHashtag} {...props} />;
};
export const FaQuoteRightIcon = ({ ...props }) => {
  return <FaIcon icon={faQuoteRight} {...props} />;
};
export const FaCubeIcon = ({ ...props }) => {
  return <FaIcon icon={faCube} {...props} />;
};
export const FaCircleCheckIcon = ({ ...props }) => {
  return <FaIcon icon={faCircleCheck} {...props} />;
};
export const FaChevronRightIcon = ({ ...props }) => {
  return <FaIcon icon={faChevronRight} {...props} />;
};

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

export const GiteeIcon: React.FC<IconSvgProps> = ({
  size = 24,
  width,
  height,
  ...props
}) => {
  return (
    <svg
      height={size || height}
      viewBox="0 -2 24 24"
      width={size || width}
      {...props}
    >
      <path
        clipRule="evenodd"
        d="M11.0592 22.1184C4.9766 22.1184 0 17.1418 0 11.0592S4.9766 0 11.0592 0s11.0592 4.9766 11.0592 11.0592-4.9766 11.0592-11.0592 11.0592zm5.5987-12.3034H10.368c-.2765 0-.553.2765-.553.553v1.3824c0 .2765.2765.553.553.553h3.8016c.2765 0 .553.2765.553.553v.2765c0 .8986-.7603 1.6589-1.6589 1.6589h-5.184c-.2765 0-.553-.2765-.553-.553V8.9856c0-.8986.7603-1.6589 1.6589-1.6589h7.6723c.2765 0 .553-.2765.553-.553v-1.3824c0-.2765-.2765-.553-.553-.553H8.9856c-2.281 0-4.0781 1.8662-4.0781 4.0781V16.5888c0 .2765.2765.553.553.553h8.087c2.0045 0 3.6634-1.6589 3.6634-3.6634v-3.1104c0-.2765-.2765-.553-.553-.553z"
        fill="currentColor"
        fillRule="evenodd"
      />
    </svg>
  );
};
