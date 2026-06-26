import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { config } from '../config/config';

let stompClient = null;
let socket = null;

/**
 * Establish a STOMP WebSocket connection using SockJS and subscribe to a user's notification topic
 * @param {string|number} userId - The ID of the authenticated user
 * @param {function} onNotification - Callback invoked when a message is received
 * @param {function} onError - Callback invoked on connection errors
 * @returns {object} The stomp client instance
 */
export const connectWebSocket = (userId, onNotification, onError) => {
  // Ensure any existing connection is disconnected before reconnecting
  disconnectWebSocket();

  try {
    console.log(`Initiating SockJS connection to ${config.WS_URL}`);
    socket = new SockJS(config.WS_URL);

    socket.onopen = () => {
      console.log('WebSocket/SockJS connection opened successfully');
    };

    stompClient = Stomp.over(socket);

    // Optional: Disable verbose STOMP debugging in browser console
    // stompClient.debug = null;

    stompClient.connect(
      {},
      (frame) => {
        console.log('STOMP Connection established successfully: ' + frame);
        
        // Subscribe to notifications for the logged-in user
        console.log(`Attempting STOMP subscription to: /topic/notifications/${userId}`);
        const subscription = stompClient.subscribe(`/topic/notifications/${userId}`, (message) => {
          console.log('Live STOMP notification message received:', message.body);
          if (message.body) {
            try {
              const notificationData = JSON.parse(message.body);
              onNotification(notificationData);
            } catch (err) {
              console.error('Failed to parse incoming STOMP message payload', err);
            }
          }
        });

        if (subscription) {
          console.log('STOMP Subscription registered successfully. Subscription ID:', subscription.id);
        }
      },
      (error) => {
        console.error('STOMP Connection error callback triggered:', error);
        if (onError) {
          onError(error);
        }
      }
    );
  } catch (err) {
    console.error('Exception caught establishing SockJS/STOMP connection:', err);
    if (onError) {
      onError(err);
    }
  }

  return stompClient;
};

/**
 * Terminate the STOMP connection and close the underlying socket
 */
export const disconnectWebSocket = () => {
  if (stompClient) {
    try {
      stompClient.disconnect(() => {
        console.log('STOMP Client disconnected successfully.');
      });
    } catch (err) {
      console.warn('Error during STOMP client disconnect:', err);
    }
    stompClient = null;
  }
  
  if (socket) {
    try {
      socket.close();
    } catch (err) {
      console.warn('Error closing underlying SockJS socket:', err);
    }
    socket = null;
  }
};
