#! /bin/bash

# q+d helper to copy mock files to the location the app expects them to be
# useful for taking the app out for a spin...

if [ ! -d /tmp/exchange ]; then
  mkdir /tmp/exchange
fi

cp ./src/test/resources/route_test_data_files/buildID.txt /tmp/exchange
cp ./src/test/resources/route_test_data_files/20201010-0000.txt /tmp/exchange