-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 19, 2025 at 10:38 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `research_platform`
--

-- --------------------------------------------------------

--
-- Table structure for table `dataset`
--

CREATE TABLE `dataset` (
  `DatasetID` int(11) NOT NULL,
  `Name` varchar(200) NOT NULL,
  `Description` text DEFAULT NULL,
  `Category` varchar(100) DEFAULT NULL,
  `PriceOrValue` decimal(10,2) DEFAULT NULL,
  `Status` enum('Public','Private','Restricted') DEFAULT 'Public',
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `PublicationID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `dataset`
--

INSERT INTO `dataset` (`DatasetID`, `Name`, `Description`, `Category`, `PriceOrValue`, `Status`, `CreatedAt`, `PublicationID`) VALUES
(1, 'Medical Images Set', 'X-ray and MRI images dataset', 'Medical', 0.00, 'Public', '2025-11-17 08:22:11', 1),
(2, 'Climate Stats 20-Year', 'Dataset containing climate data', 'Environmental', 30.00, 'Public', '2025-11-17 08:22:11', 2);

-- --------------------------------------------------------

--
-- Table structure for table `experiment`
--

CREATE TABLE `experiment` (
  `ExperimentID` int(11) NOT NULL,
  `Title` varchar(200) DEFAULT NULL,
  `Description` text DEFAULT NULL,
  `Methodology` text DEFAULT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `ResearcherID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `experiment`
--

INSERT INTO `experiment` (`ExperimentID`, `Title`, `Description`, `Methodology`, `CreatedAt`, `ResearcherID`) VALUES
(1, 'AI Tumor Detection', 'Testing AI models on tumor data', 'Supervised learning (CNN models)', '2025-11-17 08:22:33', 2),
(2, 'Temperature Prediction Test', 'Experimenting with regression methods', 'Linear & polynomial regression', '2025-11-17 08:22:33', 3),
(3, 'Algorithm Stress Test', 'Testing ML algorithms under load', 'Benchmarking framework', '2025-11-17 08:22:33', 2);

-- --------------------------------------------------------

--
-- Table structure for table `experiment_dataset`
--

CREATE TABLE `experiment_dataset` (
  `ExperimentID` int(11) NOT NULL,
  `DatasetID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `experiment_dataset`
--

INSERT INTO `experiment_dataset` (`ExperimentID`, `DatasetID`) VALUES
(1, 1),
(2, 2);

-- --------------------------------------------------------

--
-- Table structure for table `fundings`
--

CREATE TABLE `fundings` (
  `funder_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `Amount` decimal(12,2) DEFAULT NULL,
  `status` enum('Approved','Pending','Declined') DEFAULT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `ExperimentID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fundings`
--

INSERT INTO `fundings` (`funder_id`, `name`, `Amount`, `status`, `CreatedAt`, `ExperimentID`) VALUES
(1, 'WHO Foundation', 50000.00, 'Approved', '2025-11-10 07:47:59', 1),
(2, 'Green Earth Fund', 25000.00, 'Approved', '2025-11-10 07:47:59', 2),
(3, 'Tech Innovators Grant', 15000.00, 'Pending', '2025-11-10 07:47:59', 3);

-- --------------------------------------------------------

--
-- Table structure for table `project`
--

CREATE TABLE `project` (
  `ProjectID` int(11) NOT NULL,
  `Title` varchar(200) NOT NULL,
  `Description` text DEFAULT NULL,
  `Status` enum('Ongoing','Completed','Pending') DEFAULT 'Ongoing',
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `ResearcherID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `project`
--

INSERT INTO `project` (`ProjectID`, `Title`, `Description`, `Status`, `CreatedAt`, `ResearcherID`) VALUES
(1, 'AI in Healthcare', 'Using AI to diagnose diseases', 'Ongoing', '2025-11-17 08:19:14', 2),
(2, 'Climate Change Analysis', 'Statistical analysis of climate trends', 'Pending', '2025-11-17 08:19:14', 3),
(3, 'Data Mining Methods', 'Exploration of mining algorithms', 'Completed', '2025-11-17 08:20:43', 1);

-- --------------------------------------------------------

--
-- Table structure for table `publication`
--

CREATE TABLE `publication` (
  `PublicationID` int(11) NOT NULL,
  `Title` varchar(200) NOT NULL,
  `Journal` varchar(200) DEFAULT NULL,
  `Year` year(4) DEFAULT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `ProjectID` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `publication`
--

INSERT INTO `publication` (`PublicationID`, `Title`, `Journal`, `Year`, `CreatedAt`, `ProjectID`) VALUES
(1, 'AI Predictive Models', 'Journal of Medical AI', '2023', '2025-11-17 08:21:38', 1),
(2, 'Climate Temperature Forecasting', 'Environmental Science Review', '2022', '2025-11-17 08:21:38', 2);

-- --------------------------------------------------------

--
-- Table structure for table `researcher`
--

CREATE TABLE `researcher` (
  `ResearcherID` int(11) NOT NULL,
  `Username` varchar(100) NOT NULL,
  `PasswordHash` varchar(255) NOT NULL,
  `Email` varchar(150) NOT NULL,
  `FullName` varchar(150) DEFAULT NULL,
  `Role` enum('Admin','Researcher','Assistant') DEFAULT 'Researcher',
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `LastLogin` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `researcher`
--

INSERT INTO `researcher` (`ResearcherID`, `Username`, `PasswordHash`, `Email`, `FullName`, `Role`, `CreatedAt`, `LastLogin`) VALUES
(1, 'admin', 'admin123', 'admin@research.com', 'Ash Zane', 'Admin', '2025-11-10 05:08:25', '0000-00-00 00:00:00'),
(2, 'ashzane', 'admin@20', 'ashzan@gmail.com', 'Danny Geoff', 'Admin', '2025-10-23 08:05:18', NULL),
(3, 'jdowie', 'jdowie123', 'johndowie@gmail.com', 'John Dowie', 'Researcher', '2025-10-27 08:58:46', NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `dataset`
--
ALTER TABLE `dataset`
  ADD PRIMARY KEY (`DatasetID`),
  ADD KEY `PublicationID` (`PublicationID`);

--
-- Indexes for table `experiment`
--
ALTER TABLE `experiment`
  ADD PRIMARY KEY (`ExperimentID`),
  ADD KEY `ResearcherID` (`ResearcherID`);

--
-- Indexes for table `experiment_dataset`
--
ALTER TABLE `experiment_dataset`
  ADD PRIMARY KEY (`ExperimentID`,`DatasetID`),
  ADD KEY `DatasetID` (`DatasetID`);

--
-- Indexes for table `fundings`
--
ALTER TABLE `fundings`
  ADD PRIMARY KEY (`funder_id`),
  ADD KEY `fk_experiment` (`ExperimentID`);

--
-- Indexes for table `project`
--
ALTER TABLE `project`
  ADD PRIMARY KEY (`ProjectID`),
  ADD KEY `ResearcherID` (`ResearcherID`);

--
-- Indexes for table `publication`
--
ALTER TABLE `publication`
  ADD PRIMARY KEY (`PublicationID`),
  ADD KEY `ProjectID` (`ProjectID`);

--
-- Indexes for table `researcher`
--
ALTER TABLE `researcher`
  ADD PRIMARY KEY (`ResearcherID`),
  ADD UNIQUE KEY `Username` (`Username`),
  ADD UNIQUE KEY `Email` (`Email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `dataset`
--
ALTER TABLE `dataset`
  MODIFY `DatasetID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `experiment`
--
ALTER TABLE `experiment`
  MODIFY `ExperimentID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `fundings`
--
ALTER TABLE `fundings`
  MODIFY `funder_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `project`
--
ALTER TABLE `project`
  MODIFY `ProjectID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `publication`
--
ALTER TABLE `publication`
  MODIFY `PublicationID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `researcher`
--
ALTER TABLE `researcher`
  MODIFY `ResearcherID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `dataset`
--
ALTER TABLE `dataset`
  ADD CONSTRAINT `dataset_ibfk_1` FOREIGN KEY (`PublicationID`) REFERENCES `publication` (`PublicationID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `experiment`
--
ALTER TABLE `experiment`
  ADD CONSTRAINT `experiment_ibfk_1` FOREIGN KEY (`ResearcherID`) REFERENCES `researcher` (`ResearcherID`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `experiment_dataset`
--
ALTER TABLE `experiment_dataset`
  ADD CONSTRAINT `experiment_dataset_ibfk_1` FOREIGN KEY (`ExperimentID`) REFERENCES `experiment` (`ExperimentID`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `experiment_dataset_ibfk_2` FOREIGN KEY (`DatasetID`) REFERENCES `dataset` (`DatasetID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fundings`
--
ALTER TABLE `fundings`
  ADD CONSTRAINT `fk_experiment` FOREIGN KEY (`ExperimentID`) REFERENCES `experiment` (`ExperimentID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `publication`
--
ALTER TABLE `publication`
  ADD CONSTRAINT `publication_ibfk_1` FOREIGN KEY (`ProjectID`) REFERENCES `project` (`ProjectID`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
