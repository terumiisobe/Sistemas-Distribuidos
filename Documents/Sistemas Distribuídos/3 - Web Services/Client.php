<?php
include('./httpful.phar');

$url = "https://localhost/tickets";
$response = \Httpful\Request::get($url)
    ->expectsJson()
    ->withXTrivialHeader('Just as a demo')
    ->send();

echo "{$response->body->name} joined GitHub on " .
                        date('M jS', strtotime($response->body->created_at)) ."\n";
 ?>
