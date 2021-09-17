<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li>
      <a href="#usage">Usage</a>
      <ul>
        <li><a href="#run">Run</a></li>
        <li><a href="#test">Test</a></li>
      </ul>
    </li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->

## About The Project

This application opens a socket and restricts input to at most 5 concurrent clients.<br/>
Clients will connect to the Application and write one or more 9 digit numbers, each number
followed by a server-native newline sequence.<br/>
The Application writes a de-duplicated list of these numbers to a _./numbers.log_ file in no particular order.<br/>
The Application also writes to the standard output, every 10 seconds, a report with the count of unique and duplicated numbers in that period, together with the total uniques for the active run of the Application.

### Built With

* [Java 11](https://getbootstrap.com)

<!-- GETTING STARTED -->

## Getting Started

To get a local copy up and running follow these simple example steps:

### Prerequisites

1. Install Java 11 SDK (or higher)
2. Install Maven 3.8.2 (or higher)

### Installation

1. Clone the repo
   ```sh
   git clone https://github.com/illuque/tcpnumbers.git
   ```
2. Navigate to the project directory and install Maven dependencies
   ```sh
   mvn clean install
   ```

<!-- USAGE EXAMPLES -->

## Usage

### Run

To run the project, meaning setting up the server that will start listening for client messages, run the following command:

   ```sh
   mvn exec:java
   ```

### Test

#### Automated

Run automated tests with the following comments:

   ```sh
   mvn test
   ```

#### Load

On UNIX systems, performance and load tests can be run as well.<br/><br/>
For that, generate _N_ seed files with the following script. Note: _N_ configurable within the script
   ```sh
   cd load_test ; sh seed.sh
   ```

Start sending numbers from _N_ clients to the App with the following script. Numbers are read from previous seed files. Note: _N_ configurable within the script, but the App accepts 5 concurrent clients.<br/>
   ```sh
   cd load_test ; sh send_messages.sh
   ```

## License

Distributed under the MIT License. See [MIT License](https://opensource.org/licenses/MIT) for more information.

<!-- CONTACT -->

## Contact

Iago Lluque - [@illuque](https://twitter.com/illuque) - iago.lluque@gmail.com

Project Link: [https://github.com/illuque/tcpnumbers](https://github.com/illuque/tcpnumbers)