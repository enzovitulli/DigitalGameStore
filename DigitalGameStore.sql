-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: mariadb
-- Generation Time: Dec 20, 2024 at 10:49 PM
-- Server version: 11.6.2-MariaDB-ubu2404
-- PHP Version: 8.2.8

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `DigitalGameStore`
--

-- --------------------------------------------------------

--
-- Table structure for table `game`
--

CREATE TABLE `game` (
  `game_id` int(11) NOT NULL,
  `description` text NOT NULL,
  `developer` varchar(255) NOT NULL,
  `genre` set('Action','Adventure','Arcade','Battle Royale','Beat ''em Up','Builder','Card','Casual','City Builder','Comedy','Cyberpunk','Dating Sim','Detective','Dungeon Crawler','Educational','Endless Runner','Exploration','Fantasy','Fighting','First-Person Shooter (FPS)','Hack and Slash','Historical','Horror','Idle','Interactive Story','JRPG','Life Simulation','Management','Massively Multiplayer Online (MMO)','Metroidvania','Military','Minigames','Music','Mystery','Narrative','Open World','Party','Pinball','Platformer','Post-Apocalyptic','Puzzle','Racing','Real-Time Strategy (RTS)','Retro','Rhythm','Roguelike','Roguelite','Role-Playing Game (RPG)','Sandbox','Sci-Fi','Shooter','Social','Sports','Stealth','Strategy','Survival','Survival Horror','Tactical','Third-Person Shooter','Tower Defense','Trading Card','Turn-Based Strategy (TBS)','Tycoon','Visual Novel') NOT NULL,
  `lease_price` decimal(10,2) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `release_date` date NOT NULL,
  `title` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `transaction`
--

CREATE TABLE `transaction` (
  `transaction_id` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `expiry_date` date DEFAULT NULL,
  `transaction_date` date NOT NULL,
  `transaction_type` enum('Purchase','Lease') NOT NULL,
  `game_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `user_id` int(11) NOT NULL,
  `account_balance` decimal(10,2) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `game`
--
ALTER TABLE `game`
  ADD PRIMARY KEY (`game_id`);

--
-- Indexes for table `transaction`
--
ALTER TABLE `transaction`
  ADD PRIMARY KEY (`transaction_id`),
  ADD KEY `FK9psb2j1156awdh8y4v39quqr6` (`game_id`),
  ADD KEY `FKsg7jp0aj6qipr50856wf6vbw1` (`user_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `game`
--
ALTER TABLE `game`
  MODIFY `game_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `transaction`
--
ALTER TABLE `transaction`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `transaction`
--
ALTER TABLE `transaction`
  ADD CONSTRAINT `FK9psb2j1156awdh8y4v39quqr6` FOREIGN KEY (`game_id`) REFERENCES `game` (`game_id`),
  ADD CONSTRAINT `FKsg7jp0aj6qipr50856wf6vbw1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
