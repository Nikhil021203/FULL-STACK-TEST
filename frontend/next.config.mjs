/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // Allows the frontend to be bundled into the Spring Boot JAR for Render.
  output: "export",
};

export default nextConfig;
