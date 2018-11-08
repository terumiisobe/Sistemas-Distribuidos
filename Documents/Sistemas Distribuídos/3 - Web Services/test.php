<?php
use GuzzleHttp\Client;
use Psr\Http\Message\ResponseInterface;
require 'vendor/autoload.php';

$client = new Client([
    // Base URI is used with relative requests
    'base_uri' => 'http://127.0.0.1:5000/',
    // You can set any number of default request options.
    'timeout'  => 2.0,
]);
$response = $client->request('GET', 'searchFlights');
$promise = $client->getAsync('http://127.0.0.1:5000/searchFlights');
$promise->then(
    function (ResponseInterface $res) {
        echo $res->getStatusCode() . "\n";
    },
    function (RequestException $e) {
        echo $e->getMessage() . "\n";
        echo $e->getRequest()->getMethod();
    }
);
?>
