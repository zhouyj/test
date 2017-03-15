#!/bin/bash
set -e
exec "$@"
sh live-stream.sh &
sh live-profile.sh